package speck;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import speck.embeddedserver.EmbeddedServer;
import speck.embeddedserver.EmbeddedServers;
import speck.route.Routes;
import speck.ssl.SslStores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static speck.Service.ignite;

public class ServiceTest {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int NOT_FOUND_STATUS_CODE = 404;

    private Service service;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void test() {
        service = ignite();
    }

    @Test
    public void testEmbeddedServerIdentifier_defaultAndSet() {
        assertEquals("Should return defaultIdentifier()",
            EmbeddedServers.defaultIdentifier(),
            service.embeddedServerIdentifier());

        Object obj = new Object();

        service.embeddedServerIdentifier(obj);

        assertEquals("Should return expected obj",
            obj,
            service.embeddedServerIdentifier());
    }

    @Test
    public void testEmbeddedServerIdentifier_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done before route mapping has begun");

        Object obj = new Object();

        ReflectionUtils.write(service, "initialized", true);
        service.embeddedServerIdentifier(obj);
    }

    @Test(expected = HaltException.class)
    public void testHalt_whenOutParameters_thenThrowHaltException() {
        service.halt();
    }

    @Test(expected = HaltException.class)
    public void testHalt_whenStatusCode_thenThrowHaltException() {
        service.halt(NOT_FOUND_STATUS_CODE);
    }

    @Test(expected = HaltException.class)
    public void testHalt_whenBodyContent_thenThrowHaltException() {
        service.halt("error");
    }

    @Test(expected = HaltException.class)
    public void testHalt_whenStatusCodeAndBodyContent_thenThrowHaltException() {
        service.halt(NOT_FOUND_STATUS_CODE, "error");
    }

    @Test
    public void testIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = (String) ReflectionUtils.read(service, "ipAddress");
        assertEquals("IP address should be set to the IP address that was specified", IP_ADDRESS, ipAddress);
    }

    @Test
    public void testIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done before route mapping has begun");

        ReflectionUtils.write(service, "initialized", true);
        service.ipAddress(IP_ADDRESS);
    }

    @Test
    public void testSetIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = (String) ReflectionUtils.read(service, "ipAddress");
        assertEquals("IP address should be set to the IP address that was specified", IP_ADDRESS, ipAddress);
    }

    @Test
    public void testSetIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done before route mapping has begun");

        ReflectionUtils.write(service, "initialized", true);
        service.ipAddress(IP_ADDRESS);
    }

    @Test
    public void testPort_whenInitializedFalse() {
        service.port(8080);

        int port = (int) ReflectionUtils.read(service, "port");
        assertEquals("Port should be set to the Port that was specified", 8080, port);
    }

    @Test
    public void testPort_whenInitializedTrue_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done before route mapping has begun");

        ReflectionUtils.write(service, "initialized", true);
        service.port(8080);
    }

    @Test
    public void testSetPort_whenInitializedFalse() {
        service.port(8080);

        int port = (int) ReflectionUtils.read(service, "port");
        assertEquals("Port should be set to the Port that was specified", 8080, port);
    }

    @Test
    public void testSetPort_whenInitializedTrue_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done before route mapping has begun");

        ReflectionUtils.write(service, "initialized", true);
        service.port(8080);
    }

    @Test
    public void testGetPort_whenInitializedFalse_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done after route mapping has begun");

        ReflectionUtils.write(service, "initialized", false);
        service.port();
    }

    @Test
    public void testGetPort_whenInitializedTrue() {
        int expectedPort = 8080;
        ReflectionUtils.write(service, "initialized", true);
        ReflectionUtils.write(service, "port", expectedPort);

        int actualPort = service.port();

        assertEquals("Port retrieved should be the port setted", expectedPort, actualPort);
    }

    @Test
    public void testGetPort_whenInitializedTrue_Default() {
        int expectedPort = Service.SPECK_DEFAULT_PORT;
        ReflectionUtils.write(service, "initialized", true);

        int actualPort = service.port();

        assertEquals("Port retrieved should be the port setted", expectedPort, actualPort);
    }



    @Test
    public void testSecure_thenReturnNewSslStores() {
        service.secure("keyfile", "keypassword", "truststorefile", "truststorepassword");
        SslStores sslStores = (SslStores) ReflectionUtils.read(service, "sslStores");
        assertNotNull("Should return a SslStores because we configured it to have one", sslStores);
        assertEquals("Should return keystoreFile from SslStores", "keyfile", sslStores.keystoreFile());
        assertEquals("Should return keystorePassword from SslStores", "keypassword", sslStores.keystorePassword());
        assertEquals("Should return trustStoreFile from SslStores", "truststorefile", sslStores.trustStoreFile());
        assertEquals("Should return trustStorePassword from SslStores", "truststorepassword", sslStores.trustStorePassword());
    }

    @Test
    public void testSecure_whenInitializedTrue_thenThrowIllegalStateException() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("This must be done before route mapping has begun");

        ReflectionUtils.write(service, "initialized", true);
        service.secure(null, null, null, null);
    }

    @Test
    public void testSecure_whenInitializedFalse_thenThrowIllegalArgumentException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Must provide a keystore file to run secured");

        service.secure(null, null, null, null);
    }


    
    @Test(timeout = 300)
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
