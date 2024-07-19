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
package speck.embeddedserver.jdkserver;


import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import speck.ssl.SslStores;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Creates JdkServer instances.
 */
class JdkServer implements JdkServerFactory {

    public static SSLContext makeSSLContext(String keyStoreFileName,
                                            String trustStoreFileName, String keyStorePassword, String trustStorePassword)
        throws CertificateException, IOException, KeyStoreException,
        KeyManagementException, UnrecoverableKeyException {

        InputStream keyStoreStream = new FileInputStream(keyStoreFileName);
        InputStream trustStoreStream = trustStoreFileName != null ? new FileInputStream(trustStoreFileName) : new FileInputStream(keyStoreFileName);
        char[] keyStorePasswordCharArray = keyStorePassword.toCharArray();
        char[] trustStorePasswordCharArray = trustStorePassword != null ? trustStorePassword.toCharArray() : keyStorePassword.toCharArray();
        return makeSSLContext(keyStoreStream, trustStoreStream, keyStorePasswordCharArray, trustStorePasswordCharArray);

    }

    public static SSLContext makeSSLContext(InputStream keyStoreStream,
                                            InputStream trustStoreStream, char[] keyStorePassword, char[] trustStorePassword)
        throws CertificateException, IOException, KeyStoreException,
        KeyManagementException, UnrecoverableKeyException {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        KeyStore trustStore = KeyStore.getInstance("JKS");


        try {
            keyStore.load(keyStoreStream, keyStorePassword);
            trustStore.load(trustStoreStream, trustStorePassword);
        } catch (NoSuchAlgorithmException e) {
            // We expect the keystore format to be compatible with those
            // built into the Java runtime.
            throw new RuntimeException(e);
        }

        KeyManagerFactory keyManFactory;
        TrustManagerFactory trustManFactory;

        try {
            keyManFactory = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
            keyManFactory.init(keyStore, keyStorePassword);

            trustManFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManFactory.init(trustStore);
        } catch (NoSuchAlgorithmException e) {
            // We use default algorithms, so they must exist.
            throw new RuntimeException(e);
        }

        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("TLSv1.3");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        ctx.init(keyManFactory.getKeyManagers(),
            trustManFactory.getTrustManagers(), null);

        return ctx;
    }

    @Override
    public HttpServer create(int port, SslStores sslStores, Executor executor) {
        HttpServer server;
        try {
            InetSocketAddress address = new InetSocketAddress(port);
            if (sslStores == null) {
                server = HttpServer.create(address, 0);
            } else {
                server = HttpsServer.create(address, 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.setExecutor(Objects.requireNonNullElseGet(executor, Executors::newVirtualThreadPerTaskExecutor));
        if (server instanceof HttpsServer httpsServer) {
            SSLContext sslContext = null;
            try {
                sslContext = makeSSLContext(sslStores.keystoreFile(), sslStores.trustStoreFile(), sslStores.keystorePassword(), sslStores.trustStorePassword());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {

                    SSLContext c = getSSLContext();

                    // get the default parameters
                    SSLParameters sslparams = c.getDefaultSSLParameters();

                    params.setSSLParameters(sslparams);
                }
            });

        }
        return server;
    }

}
