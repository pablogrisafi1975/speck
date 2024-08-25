/*
 * Copyright 2016 - Per Wendel
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
package speck.staticfiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


import com.sun.net.httpserver.HttpExchange;
import speck.resource.AbstractFileResolvingResource;
import speck.resource.AbstractResourceHandler;
import speck.resource.ClassPathResourceHandler;
import speck.resource.ExternalResource;
import speck.resource.ExternalResourceHandler;
import speck.utils.Assert;
import speck.utils.GzipUtils;
import speck.utils.IOUtils;

/**
 * Holds the static file configuration.
 * TODO: ETAG ?
 */
public class StaticFilesConfiguration {
    private final System.Logger LOG = System.getLogger(StaticFilesConfiguration.class.getName());

    private List<AbstractResourceHandler> staticResourceHandlers = null;

    private boolean staticResourcesSet = false;
    private boolean externalStaticResourcesSet = false;

    public static StaticFilesConfiguration servletInstance = new StaticFilesConfiguration();

    private Map<String, String> customHeaders = new HashMap<>();

    /**
     * Attempt consuming using either static resource handlers or jar resource handlers
     *
     * @param httpExchange  The HTTP exchange.
     * @return true if consumed, false otherwise.
     * @throws IOException in case of IO error.
     */
    public boolean consume(HttpExchange httpExchange) throws IOException {
        try {
            if (consumeWithFileResourceHandlers(httpExchange)) {
                return true;
            }

        } catch (DirectoryTraversal.DirectoryTraversalDetection directoryTraversalDetection) {
            httpExchange.sendResponseHeaders(400, 0);
            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                outputStream.write("Bad request".getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            LOG.log(System.Logger.Level.WARNING, directoryTraversalDetection.getMessage() + " directory traversal detection for path: "
                             + httpExchange.getRequestURI().getPath());
            return true;
        }
        return false;
    }


    private boolean consumeWithFileResourceHandlers(HttpExchange httpExchange) throws IOException {
        if (staticResourceHandlers != null) {

            for (AbstractResourceHandler staticResourceHandler : staticResourceHandlers) {

                AbstractFileResolvingResource resource = staticResourceHandler.getResource(httpExchange);

                if (resource != null && resource.isReadable()) {

                    if (MimeType.shouldGuess()) {
                        httpExchange.getResponseHeaders().add(MimeType.CONTENT_TYPE, MimeType.fromResource(resource));
                    }
                    customHeaders.forEach(httpExchange.getResponseHeaders()::add); //add all user-defined headers to response



                    try (InputStream inputStream = resource.getInputStream();
                         OutputStream wrappedOutputStream = GzipUtils.checkAndWrap(httpExchange, false, 200)) {
                        IOUtils.copy(inputStream, wrappedOutputStream);
                    }catch (Exception e){
                        LOG.log(System.Logger.Level.ERROR, "Error copying file", e);
                    }

                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Clears all static file configuration
     */
    public void clear() {

        if (staticResourceHandlers != null) {
            staticResourceHandlers.clear();
            staticResourceHandlers = null;
        }

        staticResourcesSet = false;
        externalStaticResourcesSet = false;
    }
    
    public boolean isStaticResourcesSet() {
        return staticResourcesSet;
    }
    
    public boolean isExternalStaticResourcesSet() {
        return externalStaticResourcesSet;
    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public synchronized void configure(String folder) {
        Objects.requireNonNull(folder, "'folder' must not be null");

        if (!staticResourcesSet) {

            if (staticResourceHandlers == null) {
                staticResourceHandlers = new ArrayList<>();
            }

            staticResourceHandlers.add(new ClassPathResourceHandler(folder, "index.html"));
            LOG.log(System.Logger.Level.INFO, "StaticResourceHandler configured with folder = " + folder);
            staticResourcesSet = true;
        }
    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public synchronized void configureExternal(String folder) {
        Objects.requireNonNull(folder, "'folder' must not be null");

        if (!externalStaticResourcesSet) {
            try {
                ExternalResource resource = new ExternalResource(folder);
                if (!resource.getFile().isDirectory()) {
                    LOG.log(System.Logger.Level.ERROR, "External Static resource location must be a folder");
                    return;
                }

                if (staticResourceHandlers == null) {
                    staticResourceHandlers = new ArrayList<>();
                }
                staticResourceHandlers.add(new ExternalResourceHandler(folder, "index.html"));
                LOG.log(System.Logger.Level.INFO, "External StaticResourceHandler configured with folder = " + folder);
            } catch (IOException e) {
                LOG.log(System.Logger.Level.ERROR, "Error when creating external StaticResourceHandler", e);
            }

            externalStaticResourcesSet = true;
        }
    }

    public static StaticFilesConfiguration create() {
        return new StaticFilesConfiguration();
    }

    public void setExpireTimeSeconds(long expireTimeSeconds) {
        customHeaders.put("Cache-Control", "private, max-age=" + expireTimeSeconds);
        customHeaders.put("Expires", new Date(System.currentTimeMillis() + (expireTimeSeconds * 1000)).toString());
    }

    public void putCustomHeaders(Map<String, String> headers) {
        customHeaders.putAll(headers);
    }

    public void putCustomHeader(String key, String value) {
        customHeaders.put(key, value);
    }
}
