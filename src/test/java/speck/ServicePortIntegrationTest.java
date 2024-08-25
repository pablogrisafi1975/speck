package speck;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import speck.util.SpeckTestUtil;

import static speck.Service.ignite;

/**
 * Created by Tom on 08/02/2017.
 */
public class ServicePortIntegrationTest {

    private static Service service;
    private static final System.Logger logger = System.getLogger(ServicePortIntegrationTest.class.getName());;

    @BeforeAll
    public static void setUpClass() throws Exception {
        service = ignite();
        service.port(0);

        service.get("/hi", (q, a) -> "Hello World!");

        service.awaitInitialization();
    }

    @Test
    public void testGetPort_withRandomPort() throws Exception {
        int actualPort = service.port();

        logger.log(System.Logger.Level.INFO, "got port ");

        SpeckTestUtil testUtil = new SpeckTestUtil(actualPort);

        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertEquals(200, response.status);
        assertEquals("Hello World!", response.body);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        service.stop();
    }
}
