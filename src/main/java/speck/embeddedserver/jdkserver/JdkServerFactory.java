package speck.embeddedserver.jdkserver;

import com.sun.net.httpserver.HttpServer;
import speck.ssl.SslStores;

import java.util.concurrent.Executor;

/**
 * This interface can be implemented to provide custom Jetty server instances
 * with specific settings or features.
 */
public interface JdkServerFactory {
    HttpServer create(int port, SslStores sslStores, Executor executor);
}
