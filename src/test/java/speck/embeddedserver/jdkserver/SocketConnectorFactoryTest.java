package speck.embeddedserver.jdkserver;


import org.junit.jupiter.api.Test;
import speck.ReflectionUtils;
import speck.ssl.SslStores;

import java.util.Map;





public class SocketConnectorFactoryTest {
/*
    @Test
    public void testCreateSocketConnector_whenServerIsNull_thenThrowException() {

        try {
            SocketConnectorFactory.createSocketConnector(null, "host", 80, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("'server' must not be null", ex.getMessage());
        }
    }


    @Test
    public void testCreateSocketConnector_whenHostIsNull_thenThrowException() {

        Server server = new Server();

        try {
            SocketConnectorFactory.createSocketConnector(server, null, 80, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("'host' must not be null", ex.getMessage());
        }
    }

    @Test
    public void testCreateSocketConnector() {

        final String host = "localhost";
        final int port = 8888;

        Server server = new Server();
        ServerConnector serverConnector = SocketConnectorFactory.createSocketConnector(server, "localhost", 8888, true);

        String internalHost = (String) ReflectionUtils.read(serverConnector, "_host");
        int internalPort = (int) ReflectionUtils.read(serverConnector, "_port");
        Server internalServerConnector = (Server) ReflectionUtils.read(serverConnector, "_server");

        assertEquals("Server Connector Host should be set to the specified server", host, internalHost);
        assertEquals("Server Connector Port should be set to the specified port", port, internalPort);
        assertEquals("Server Connector Server should be set to the specified server", internalServerConnector, server);
    }


    @Test
    public void testCreateSecureSocketConnector_whenServerIsNull() {

        try {
            SocketConnectorFactory.createSecureSocketConnector(null, "localhost", 80, null, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("'server' must not be null", ex.getMessage());
        }
    }

    @Test
    public void testCreateSecureSocketConnector_whenHostIsNull() {

        Server server = new Server();

        try {
            SocketConnectorFactory.createSecureSocketConnector(server, null, 80, null, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("'host' must not be null", ex.getMessage());
        }
    }

    @Test
    public void testCreateSecureSocketConnector_whenSslStoresIsNull() {

        Server server = new Server();

        try {
            SocketConnectorFactory.createSecureSocketConnector(server, "localhost", 80, null, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("'sslStores' must not be null", ex.getMessage());
        }
    }


    @Test
    // @PrepareForTest({ServerConnector.class})
    public void testCreateSecureSocketConnector() throws Exception {

        final String host = "localhost";
        final int port = 8888;

        final String keystoreFile = "keystoreFile.jks";
        final String keystorePassword = "keystorePassword";
        final String truststoreFile = "truststoreFile.jks";
        final String trustStorePassword = "trustStorePassword";

        SslStores sslStores = SslStores.create(keystoreFile, keystorePassword, truststoreFile, trustStorePassword);

        Server server = new Server();

        ServerConnector serverConnector = SocketConnectorFactory.createSecureSocketConnector(server, host, port, sslStores, true);

        String internalHost = (String) ReflectionUtils.read(serverConnector, "_host");
        int internalPort = (int) ReflectionUtils.read(serverConnector, "_port");

        assertEquals("Server Connector Host should be set to the specified server", host, internalHost);
        assertEquals("Server Connector Port should be set to the specified port", port, internalPort);

        Map<String, ConnectionFactory> factories = (Map<String, ConnectionFactory>) ReflectionUtils.read(serverConnector, "_factories");

        assertTrue("Should return true because factory for SSL should have been set",
            factories.containsKey("ssl") && factories.get("ssl") != null);

        SslConnectionFactory sslConnectionFactory = (SslConnectionFactory) factories.get("ssl");
        SslContextFactory sslContextFactory = sslConnectionFactory.getSslContextFactory();

        assertEquals("Should return the Keystore file specified", keystoreFile,
            sslContextFactory.getKeyStoreResource().getFile().getName());

        assertEquals("Should return the Truststore file specified", truststoreFile,
            sslContextFactory.getTrustStoreResource().getFile().getName());

    }
*/
}
