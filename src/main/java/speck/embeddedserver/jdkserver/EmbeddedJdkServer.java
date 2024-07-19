/*
 * Copyright 2011- Per Wendel
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
package speck.embeddedserver.jdkserver;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import speck.embeddedserver.EmbeddedServer;
import speck.ssl.SslStores;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Speck server implementation
 *
 * @author Per Wendel
 */
public class EmbeddedJdkServer implements EmbeddedServer {

    private static final int SPECK_DEFAULT_PORT = 4567;
    private static final String NAME = "Speck";
    private static final System.Logger LOG = System.getLogger(EmbeddedJdkServer.class.getName());
    private final JdkServerFactory serverFactory;
    private final HttpHandler handler;
    private HttpServer server;
    private Executor executor = null;

    public EmbeddedJdkServer(JdkServerFactory serverFactory, HttpHandler handler) {
        this.serverFactory = serverFactory;
        this.handler = handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int ignite(String host,
                      int port,
                      SslStores sslStores) throws Exception {

        if (port == 0) {
            try (ServerSocket s = new ServerSocket(0)) {
                port = s.getLocalPort();
            } catch (IOException e) {
                LOG.log(System.Logger.Level.ERROR, "Could not get first available port (port set to 0), using default: {0}", SPECK_DEFAULT_PORT);
                port = SPECK_DEFAULT_PORT;
            }
        }

        // Create instance of jdk server with either default or supplied executor
        server = serverFactory.create(port, sslStores, executor);


        server.createContext("/", handler);


        LOG.log(System.Logger.Level.INFO, "== {0} has ignited ...", NAME);
        LOG.log(System.Logger.Level.INFO, ">> Listening on {0}:{1}", host, port);
        server.start();
        return port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void join() throws InterruptedException {
        server.getExecutor();
        //server.join();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void extinguish() {
        LOG.log(System.Logger.Level.INFO, ">>> {0} shutting down ...", NAME);
        try {
            if (server != null) {
                server.stop(0);
            }
        } catch (Exception e) {
            LOG.log(System.Logger.Level.ERROR, "stop failed", e);
            System.exit(100); // NOSONAR
        }
        LOG.log(System.Logger.Level.INFO, "done");
    }

    @Override
    public int activeThreadCount() {
        if (server == null) {
            return 0;
        }

        if (server.getExecutor() instanceof ThreadPoolExecutor threadPoolExecutor) {
            return threadPoolExecutor.getActiveCount();
        }

        return -1;
    }

    /**
     * Sets optional Executor pool for jdk server.  This is useful for overriding the default thread pool
     *
     * @param executor executor
     * @return Builder pattern - returns this instance
     */
    public EmbeddedJdkServer withExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }
}
