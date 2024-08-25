package speck;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import speck.embeddedserver.EmbeddedServer;
import speck.embeddedserver.EmbeddedServers;
import speck.route.Routes;
import speck.ssl.SslStores;

import static org.junit.jupiter.api.Assertions.*;
import static speck.Service.ignite;

public class ServiceTest {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int NOT_FOUND_STATUS_CODE = 404;

    private Service service;

    @BeforeEach
    public void test() {
        service = ignite();
    }

    @Test
    public void testEmbeddedServerIdentifier_defaultAndSet() {
        assertEquals(
            EmbeddedServers.defaultIdentifier(),
            service.embeddedServerIdentifier(),
            "Should return defaultIdentifier()");

        Object obj = new Object();

        service.embeddedServerIdentifier(obj);

        assertEquals(
            obj,
            service.embeddedServerIdentifier(),
            "Should return expected obj");
    }

    @Test
    public void testEmbeddedServerIdentifier_thenThrowIllegalStateException() {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            Object obj = new Object();

            ReflectionUtils.write(service, "initialized", true);
            service.embeddedServerIdentifier(obj);
        });
        assertEquals(thrown.getMessage(), "This must be done before route mapping has begun");


    }

    @Test
    public void testHalt_whenOutParameters_thenThrowHaltException() {
        assertThrows(HaltException.class, () -> {
            service.halt();
        });

    }

    @Test
    public void testHalt_whenStatusCode_thenThrowHaltException() {
        assertThrows(HaltException.class, () -> {
            service.halt(NOT_FOUND_STATUS_CODE);
        });
    }

    @Test
    public void testHalt_whenBodyContent_thenThrowHaltException() {
        assertThrows(HaltException.class, () -> {
            service.halt("error");
        });

    }

    @Test
    public void testHalt_whenStatusCodeAndBodyContent_thenThrowHaltException() {
        assertThrows(HaltException.class, () -> {
            service.halt(NOT_FOUND_STATUS_CODE, "error");
        });
    }

    @Test
    public void testIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = (String) ReflectionUtils.read(service, "ipAddress");
        assertEquals( IP_ADDRESS, ipAddress, "IP address should be set to the IP address that was specified");
    }

    @Test
    public void testIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        var thrown = assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.write(service, "initialized", true);
            service.ipAddress(IP_ADDRESS);
        });
        assertEquals("This must be done before route mapping has begun", thrown.getMessage());
    }

    @Test
    public void testSetIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = (String) ReflectionUtils.read(service, "ipAddress");
        assertEquals( IP_ADDRESS, ipAddress, "IP address should be set to the IP address that was specified");
    }

    @Test
    public void testSetIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        var thrown = assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.write(service, "initialized", true);
            service.ipAddress(IP_ADDRESS);
        });
        assertEquals("This must be done before route mapping has begun", thrown.getMessage());
    }

    @Test
    public void testPort_whenInitializedFalse() {
        service.port(8080);

        int port = (int) ReflectionUtils.read(service, "port");
        assertEquals(8080, port, "Port should be set to the Port that was specified");
    }

    @Test
    public void testPort_whenInitializedTrue_thenThrowIllegalStateException() {
        var thrown = assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.write(service, "initialized", true);
            service.port(8080);
        });
        assertEquals("This must be done before route mapping has begun", thrown.getMessage());
    }

    @Test
    public void testSetPort_whenInitializedFalse() {
        service.port(8080);

        int port = (int) ReflectionUtils.read(service, "port");
        assertEquals(8080, port, "Port should be set to the Port that was specified");
    }

    @Test
    public void testSetPort_whenInitializedTrue_thenThrowIllegalStateException() {
        var thrown = assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.write(service, "initialized", true);
            service.port(8080);
        });
        assertEquals("This must be done before route mapping has begun", thrown.getMessage());
    }

    @Test
    public void testGetPort_whenInitializedFalse_thenThrowIllegalStateException() {
        var thrown = assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.write(service, "initialized", false);
            service.port();
        });
        assertEquals("This must be done after route mapping has begun", thrown.getMessage());
    }

    @Test
    public void testGetPort_whenInitializedTrue() {
        int expectedPort = 8080;
        ReflectionUtils.write(service, "initialized", true);
        ReflectionUtils.write(service, "port", expectedPort);

        int actualPort = service.port();

        assertEquals(expectedPort, actualPort, "Port retrieved should be the port setted");
    }

    @Test
    public void testGetPort_whenInitializedTrue_Default() {
        int expectedPort = Service.SPECK_DEFAULT_PORT;
        ReflectionUtils.write(service, "initialized", true);

        int actualPort = service.port();

        assertEquals(expectedPort, actualPort, "Port retrieved should be the port setted");
    }


    @Test
    public void testSecure_thenReturnNewSslStores() {
        service.secure("keyfile", "keypassword", "truststorefile", "truststorepassword");
        SslStores sslStores = (SslStores) ReflectionUtils.read(service, "sslStores");
        assertNotNull(sslStores, "Should return a SslStores because we configured it to have one");
        assertEquals("keyfile", sslStores.keystoreFile(), "Should return keystoreFile from SslStores");
        assertEquals("keypassword", sslStores.keystorePassword(), "Should return keystorePassword from SslStores");
        assertEquals("truststorefile", sslStores.trustStoreFile(), "Should return trustStoreFile from SslStores");
        assertEquals("truststorepassword", sslStores.trustStorePassword(), "Should return trustStorePassword from SslStores");
    }

    @Test
    public void testSecure_whenInitializedTrue_thenThrowIllegalStateException() {
        var thrown = assertThrows(IllegalStateException.class, () -> {
            ReflectionUtils.write(service, "initialized", true);
            service.secure(null, null, null, null);

        });
        assertEquals("This must be done before route mapping has begun", thrown.getMessage());
    }

    @Test
    public void testSecure_whenInitializedFalse_thenThrowIllegalArgumentException() {
        var thrown = assertThrows(IllegalArgumentException.class, () -> {
            service.secure(null, null, null, null);

        });
        assertEquals("Must provide a keystore file to run secured", thrown.getMessage());

    }


    @Test
    public void stopExtinguishesServer() {
        Service service = Service.ignite();
        Routes routes = Mockito.mock(Routes.class);
        EmbeddedServer server = Mockito.mock(EmbeddedServer.class);
        service.routes = routes;
        service.server = server;
        service.initialized = true;
        service.stop();
        try {
            // yes, this is ugly and forces to set a test timeout as a precaution :(
            while (service.initialized) {
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Mockito.verify(server).extinguish();
    }

    @Test
    public void awaitStopBlocksUntilExtinguished() {
        Service service = Service.ignite();
        Routes routes = Mockito.mock(Routes.class);
        EmbeddedServer server = Mockito.mock(EmbeddedServer.class);
        service.routes = routes;
        service.server = server;
        service.initialized = true;
        service.stop();
        service.awaitStop();
        Mockito.verify(server).extinguish();
        assertFalse(service.initialized);
    }


}
