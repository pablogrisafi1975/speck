/*
 * Copyright 2015 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package speck;

import speck.embeddedserver.EmbeddedServer;
import speck.embeddedserver.EmbeddedServers;
import speck.route.HttpMethod;
import speck.route.Routes;
import speck.routematch.RouteMatch;
import speck.ssl.SslStores;
import speck.staticfiles.MimeType;
import speck.staticfiles.StaticFilesConfiguration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Represents a Speck server "session".
 * If a user wants multiple 'Specks' in his application the method {@link Service#ignite()} should be statically
 * imported and used to create instances. The instance should typically be named so when prefixing the 'routing' methods
 * the semantic makes sense. For example 'http' is a good variable name since when adding routes it would be:
 * Service http = ignite();
 * ...
 * http.get("/hello", (q, a) {@literal ->} "Hello World");
 */
public final class Service extends Routable {
    public static final int SPECK_DEFAULT_PORT = 4567;
    protected static final String DEFAULT_ACCEPT_TYPE = "*/*";
    private static final System.Logger LOG = System.getLogger("speck.Speck");
    public final Redirect redirect;
    public final StaticFiles staticFiles;
    private final StaticFilesConfiguration staticFilesConfiguration;
    private final ExceptionMapper exceptionMapper = new ExceptionMapper();
    protected boolean initialized = false;
    protected int port = SPECK_DEFAULT_PORT;
    protected String ipAddress = "0.0.0.0";
    protected SslStores sslStores;
    protected Executor executor = null;

    protected EmbeddedServer server;
    protected Deque<String> pathDeque = new ArrayDeque<>();
    protected Routes routes;
    private CountDownLatch initLatch = new CountDownLatch(1);
    private CountDownLatch stopLatch = new CountDownLatch(0);
    private Object embeddedServerIdentifier = EmbeddedServers.defaultIdentifier();
    // default exception handler during initialization phase
    private Consumer<Exception> initExceptionHandler = (e) -> {
        LOG.log(System.Logger.Level.ERROR, "ignite failed", e);
        System.exit(100);
    };

    private Service() {
        redirect = Redirect.create(this);
        staticFiles = new StaticFiles();
        staticFilesConfiguration = StaticFilesConfiguration.create();
    }

    /**
     * Creates a new Service (a Speck instance). This should be used instead of the static API if the user wants
     * multiple services in one process.
     *
     * @return the newly created object
     */
    public static Service ignite() {
        return new Service();
    }

    /**
     * Set the identifier used to select the EmbeddedServer;
     * null for the default.
     *
     * @param obj the identifier passed to {@link EmbeddedServers}.
     */
    public synchronized void embeddedServerIdentifier(Object obj) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }
        embeddedServerIdentifier = obj;
    }

    /**
     * Get the identifier used to select the EmbeddedServer;
     * null for the default.
     */
    public synchronized Object embeddedServerIdentifier() {
        return embeddedServerIdentifier;
    }

    /**
     * Set the IP address that Speck should listen on. If not called the default
     * address is '0.0.0.0'. This has to be called before any route mapping is
     * done.
     *
     * @param ipAddress The ipAddress
     * @return the object with IP address set
     */
    public synchronized Service ipAddress(String ipAddress) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }
        this.ipAddress = ipAddress;

        return this;
    }

    /**
     * Set the port that Speck should listen on. If not called the default port
     * is 4567. This has to be called before any route mapping is done.
     * If provided port = 0 then the an arbitrary available port will be used.
     *
     * @param port The port number
     * @return the object with port set
     */
    public synchronized Service port(int port) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }
        this.port = port;
        return this;
    }

    /**
     * Retrieves the port that Speck is listening on.
     *
     * @return The port Speck server is listening on.
     * @throws IllegalStateException when the server is not started
     */
    public synchronized int port() {
        if (initialized) {
            return port;
        } else {
            throw new IllegalStateException("This must be done after route mapping has begun");
        }
    }

    /**
     * Set the connection to be secure, using the specified keystore and
     * truststore. This has to be called before any route mapping is done. You
     * have to supply a keystore file, truststore file is optional (keystore
     * will be reused). By default, client certificates are not checked.
     * This method is only relevant when using embedded Jetty servers. It should
     * not be used if you are using Servlets, where you will need to secure the
     * connection in the servlet container
     *
     * @param keystoreFile       The keystore file location as string
     * @param keystorePassword   the password for the keystore
     * @param truststoreFile     the truststore file location as string, leave null to reuse
     *                           keystore
     * @param truststorePassword the trust store password
     * @return the object with connection set to be secure
     */
    public synchronized Service secure(String keystoreFile,
                                       String keystorePassword,
                                       String truststoreFile,
                                       String truststorePassword) {
        return secure(keystoreFile, keystorePassword, null, truststoreFile, truststorePassword, false);
    }

    /**
     * Set the connection to be secure, using the specified keystore and
     * truststore. This has to be called before any route mapping is done. You
     * have to supply a keystore file, truststore file is optional (keystore
     * will be reused). By default, client certificates are not checked.
     * This method is only relevant when using embedded Jetty servers. It should
     * not be used if you are using Servlets, where you will need to secure the
     * connection in the servlet container
     *
     * @param keystoreFile       The keystore file location as string
     * @param keystorePassword   the password for the keystore
     * @param certAlias          the default certificate Alias
     * @param truststoreFile     the truststore file location as string, leave null to reuse
     *                           keystore
     * @param truststorePassword the trust store password
     * @return the object with connection set to be secure
     */
    public synchronized Service secure(String keystoreFile,
                                       String keystorePassword,
                                       String certAlias,
                                       String truststoreFile,
                                       String truststorePassword) {
        return secure(keystoreFile, keystorePassword, certAlias, truststoreFile, truststorePassword, false);
    }

    /**
     * Set the connection to be secure, using the specified keystore and
     * truststore. This has to be called before any route mapping is done. You
     * have to supply a keystore file, truststore file is optional (keystore
     * will be reused).
     * This method is only relevant when using embedded Jetty servers. It should
     * not be used if you are using Servlets, where you will need to secure the
     * connection in the servlet container
     *
     * @param keystoreFile       The keystore file location as string
     * @param keystorePassword   the password for the keystore
     * @param truststoreFile     the truststore file location as string, leave null to reuse
     *                           keystore
     * @param needsClientCert    Whether to require client certificate to be supplied in
     *                           request
     * @param truststorePassword the trust store password
     * @return the object with connection set to be secure
     */
    public synchronized Service secure(String keystoreFile,
                                       String keystorePassword,
                                       String truststoreFile,
                                       String truststorePassword,
                                       boolean needsClientCert) {
        return secure(keystoreFile, keystorePassword, null, truststoreFile, truststorePassword, needsClientCert);
    }

    /**
     * Set the connection to be secure, using the specified keystore and
     * truststore. This has to be called before any route mapping is done. You
     * have to supply a keystore file, truststore file is optional (keystore
     * will be reused).
     * This method is only relevant when using embedded Jetty servers. It should
     * not be used if you are using Servlets, where you will need to secure the
     * connection in the servlet container
     *
     * @param keystoreFile       The keystore file location as string
     * @param keystorePassword   the password for the keystore
     * @param certAlias          the default certificate Alias
     * @param truststoreFile     the truststore file location as string, leave null to reuse
     *                           keystore
     * @param needsClientCert    Whether to require client certificate to be supplied in
     *                           request
     * @param truststorePassword the trust store password
     * @return the object with connection set to be secure
     */
    public synchronized Service secure(String keystoreFile,
                                       String keystorePassword,
                                       String certAlias,
                                       String truststoreFile,
                                       String truststorePassword,
                                       boolean needsClientCert) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }

        if (keystoreFile == null) {
            throw new IllegalArgumentException(
                "Must provide a keystore file to run secured");
        }

        sslStores = SslStores.create(keystoreFile, keystorePassword, certAlias, truststoreFile, truststorePassword, needsClientCert);
        return this;
    }


    /**
     * Configures the embedded web server's executor.
     *
     * @param executor        executor
     * @return the object with the embedded web server's thread pool configured
     */
    public synchronized Service executor(Executor executor) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }

        this.executor = executor;
        return this;
    }

    /**
     * Sets the folder in classpath serving static files. Observe: this method
     * must be called before all other methods.
     *
     * @param folder the folder in classpath.
     * @return the object with folder set
     */
    public synchronized Service staticFileLocation(String folder) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }

        if (!staticFilesConfiguration.isStaticResourcesSet()) {
            staticFilesConfiguration.configure(folder);
        } else {
            LOG.log(System.Logger.Level.WARNING, "Static file location has already been set");
        }
        return this;
    }

    /**
     * Sets the external folder serving static files. <b>Observe: this method
     * must be called before all other methods.</b>
     *
     * @param externalFolder the external folder serving static files.
     * @return the object with external folder set
     */
    public synchronized Service externalStaticFileLocation(String externalFolder) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }

        if (!staticFilesConfiguration.isExternalStaticResourcesSet()) {
            staticFilesConfiguration.configureExternal(externalFolder);
        } else {
            LOG.log(System.Logger.Level.WARNING, "External static file location has already been set");
        }
        return this;
    }

    /**
     * Unmaps a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path and unmaps any matches that are found.
     *
     * @param path the route path
     * @return <tt>true</tt> if this is a matching route which has been previously routed
     * @throws IllegalArgumentException if <tt>path</tt> is null or blank
     */
    public boolean unmap(String path) {
        return routes.remove(path);
    }

    /**
     * Unmaps a particular route from the collection of those that have been previously routed.
     * Search for previously established routes using the given path and HTTP method, unmaps any
     * matches that are found.
     *
     * @param path       the route path
     * @param httpMethod the http method
     * @return <tt>true</tt> if this is a matching route that has been previously routed
     * @throws IllegalArgumentException if <tt>path</tt> is null or blank or if <tt>httpMethod</tt> is null, blank,
     *                                  or an invalid HTTP method
     */
    public boolean unmap(String path, String httpMethod) {
        return routes.remove(path, httpMethod);
    }


    /**
     * Maps 404 errors to the provided custom page
     *
     * @param page the custom 404 error page.
     */
    public synchronized void notFound(String page) {
        CustomErrorPages.add(404, page);
    }

    /**
     * Maps 500 internal server errors to the provided custom page
     *
     * @param page the custom 500 internal server error page.
     */
    public synchronized void internalServerError(String page) {
        CustomErrorPages.add(500, page);
    }

    /**
     * Maps 404 errors to the provided route.
     */
    public synchronized void notFound(Route route) {
        CustomErrorPages.add(404, route);
    }

    /**
     * Maps 500 internal server errors to the provided route.
     */
    public synchronized void internalServerError(Route route) {
        CustomErrorPages.add(500, route);
    }

    /**
     * Waits for the speck server to be initialized.
     * If it's already initialized will return immediately
     */
    public void awaitInitialization() {
        if (!initialized) {
            throw new IllegalStateException("Server has not been properly initialized");
        }

        try {
            initLatch.await();
        } catch (InterruptedException e) {
            LOG.log(System.Logger.Level.INFO, "Interrupted by another thread");
            Thread.currentThread().interrupt();
        }
    }

    private void throwBeforeRouteMappingException() {
        throw new IllegalStateException(
            "This must be done before route mapping has begun");
    }


    /**
     * Stops the Speck server and clears all routes.
     */
    public synchronized void stop() {
        if (!initialized) {
            return;
        }
        initiateStop();
    }

    /**
     * Waits for the Speck server to stop.
     * <b>Warning:</b> this method should not be called from a request handler.
     */
    public void awaitStop() {
        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            LOG.log(System.Logger.Level.WARNING, "Interrupted by another thread");
            Thread.currentThread().interrupt();
        }
    }

    private void initiateStop() {
        stopLatch = new CountDownLatch(1);
        Thread stopThread = new Thread(() -> {
            if (server != null) {
                server.extinguish();
                initLatch = new CountDownLatch(1);
            }

            routes.clear();
            exceptionMapper.clear();
            staticFilesConfiguration.clear();
            initialized = false;
            stopLatch.countDown();
        });
        stopThread.start();
    }

    /**
     * Add a path-prefix to the routes declared in the routeGroup
     * The path() method adds a path-fragment to a path-stack, adds
     * routes from the routeGroup, then pops the path-fragment again.
     * It's used for separating routes into groups, for example:
     * path("/api/email", () -> {
     * ....post("/add",       EmailApi::addEmail);
     * ....put("/change",     EmailApi::changeEmail);
     * ....etc
     * });
     * Multiple path() calls can be nested.
     *
     * @param path       the path to prefix routes with
     * @param routeGroup group of routes (can also contain path() calls)
     */
    public void path(String path, RouteGroup routeGroup) {
        pathDeque.addLast(path);
        routeGroup.addRoutes();
        pathDeque.removeLast();
    }

    public String getPaths() {
        return pathDeque.stream().collect(Collectors.joining(""));
    }

    /**
     * @return all routes information from this service
     */
    public List<RouteMatch> routes() {
        return routes.findAll();
    }

    @Override
    public void addRoute(HttpMethod httpMethod, RouteImpl route) {
        init();
        routes.add(httpMethod, route.withPrefix(getPaths()));
    }

    @Override
    public void addFilter(HttpMethod httpMethod, FilterImpl filter) {
        init();
        routes.add(httpMethod, filter.withPrefix(getPaths()));
    }

    public synchronized void init() {
        if (!initialized) {

            initializeRouteMatcher();


            new Thread(() -> {
                try {
                    EmbeddedServers.initialize();

                    if (embeddedServerIdentifier == null) {
                        embeddedServerIdentifier = EmbeddedServers.defaultIdentifier();
                    }

                    server = EmbeddedServers.create(embeddedServerIdentifier,
                        routes,
                        exceptionMapper,
                        staticFilesConfiguration,
                        false);

                    port = server.ignite(
                        ipAddress,
                        port,
                        sslStores
                    );
                } catch (Exception e) {
                    initExceptionHandler.accept(e);
                }
                try {
                    initLatch.countDown();
                    server.join();
                } catch (InterruptedException e) {
                    LOG.log(System.Logger.Level.ERROR, "server interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }).start();

            initialized = true;
        }
    }

    private void initializeRouteMatcher() {
        routes = Routes.create();
    }

    /**
     * @return The approximate number of currently active threads in the embedded Jetty server
     */
    public synchronized int activeThreadCount() {
        if (server != null) {
            return server.activeThreadCount();
        }
        return 0;
    }

    //////////////////////////////////////////////////
    // EXCEPTION mapper
    //////////////////////////////////////////////////

    /**
     * Maps an exception handler to be executed when an exception occurs during routing
     *
     * @param exceptionClass the exception class
     * @param handler        The handler
     */
    public synchronized <T extends Exception> void exception(Class<T> exceptionClass, ExceptionHandler<? super T> handler) {
        // wrap
        ExceptionHandlerImpl<T> wrapper = new ExceptionHandlerImpl<>(exceptionClass) {
            @Override
            public void handle(T exception, Request request, Response response) {
                handler.handle(exception, request, response);
            }
        };

        exceptionMapper.map(exceptionClass, wrapper);
    }

    //////////////////////////////////////////////////
    // HALT methods
    //////////////////////////////////////////////////

    /**
     * Immediately stops a request within a filter or route
     * NOTE: When using this don't catch exceptions of type HaltException, or if catched, re-throw otherwise
     * halt will not work
     *
     * @return HaltException object
     */
    public HaltException halt() {
        throw new HaltException();
    }

    /**
     * Immediately stops a request within a filter or route with specified status code
     * NOTE: When using this don't catch exceptions of type HaltException, or if catched, re-throw otherwise
     * halt will not work
     *
     * @param status the status code
     * @return HaltException object with status code set
     */
    public HaltException halt(int status) {
        throw new HaltException(status);
    }

    /**
     * Immediately stops a request within a filter or route with specified body content
     * NOTE: When using this don't catch exceptions of type HaltException, or if catched, re-throw otherwise
     * halt will not work
     *
     * @param body The body content
     * @return HaltException object with body set
     */
    public HaltException halt(String body) {
        throw new HaltException(body);
    }

    /**
     * Immediately stops a request within a filter or route with specified status code and body content
     * NOTE: When using this don't catch exceptions of type HaltException, or if catched, re-throw otherwise
     * halt will not work
     *
     * @param status The status code
     * @param body   The body content
     * @return HaltException object with status and body set
     */
    public HaltException halt(int status, String body) {
        throw new HaltException(status, body);
    }


    /**
     * Overrides default exception handler during initialization phase
     *
     * @param initExceptionHandler The custom init exception handler
     */
    public void initExceptionHandler(Consumer<Exception> initExceptionHandler) {
        if (initialized) {
            throwBeforeRouteMappingException();
        }
        this.initExceptionHandler = initExceptionHandler;
    }

    /**
     * Provides static files utility methods.
     */
    public final class StaticFiles {

        /**
         * Sets the folder in classpath serving static files. Observe: this method
         * must be called before all other methods.
         *
         * @param folder the folder in classpath.
         */
        public void location(String folder) {
            staticFileLocation(folder);
        }

        /**
         * Sets the external folder serving static files. <b>Observe: this method
         * must be called before all other methods.</b>
         *
         * @param externalFolder the external folder serving static files.
         */
        public void externalLocation(String externalFolder) {
            externalStaticFileLocation(externalFolder);
        }

        /**
         * Puts custom headers for static resources. If the headers previously contained mapping for
         * a specific key in the provided headers map, the old value is replaced by the specified value.
         *
         * @param headers the headers to set on static resources
         */
        public void headers(Map<String, String> headers) {
            staticFilesConfiguration.putCustomHeaders(headers);
        }

        /**
         * Puts custom header for static resources. If the headers previously contained a mapping for
         * the key, the old value is replaced by the specified value.
         *
         * @param key   the key
         * @param value the value
         */
        public void header(String key, String value) {
            staticFilesConfiguration.putCustomHeader(key, value);
        }

        /**
         * Sets the expire-time for static resources
         *
         * @param seconds the expire time in seconds
         */
        @Experimental("Functionality will not be removed. The API might change")
        public void expireTime(long seconds) {
            staticFilesConfiguration.setExpireTimeSeconds(seconds);
        }

        /**
         * Maps an extension to a mime-type. This will overwrite any previous mappings.
         *
         * @param extension the extension to be mapped
         * @param mimeType  the mime-type for the extension
         */
        public void registerMimeType(String extension, String mimeType) {
            MimeType.register(extension, mimeType);
        }

        /**
         * Disables the automatic setting of Content-Type header made from a guess based on extension.
         */
        public void disableMimeTypeGuessing() {
            MimeType.disableGuessing();
        }

    }
}
