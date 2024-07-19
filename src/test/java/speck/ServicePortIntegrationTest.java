package speck;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import speck.util.SpeckTestUtil;

import static speck.Service.ignite;

/**
 * Created by Tom on 08/02/2017.
 */
public class ServicePortIntegrationTest {

    private static Service service;
    private static final System.Logger logger = System.getLogger(ServicePortIntegrationTest.class.getName());;

    @BeforeClass
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
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("Hello World!", response.body);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        service.stop();
    }
}
