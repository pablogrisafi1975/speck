package speck;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.util.SpeckTestUtil;

import static speck.Speck.after;
import static speck.Speck.before;
import static speck.Speck.post;

public class BodyAvailabilityTest {

    private static final System.Logger LOGGER = System.getLogger(BodyAvailabilityTest.class.getName());;

    private static final String BODY_CONTENT = "the body content";
    
    private static SpeckTestUtil testUtil;

    private final int HTTP_OK = 200;
    
    private static String beforeBody = null;
    private static String routeBody = null;
    private static String afterBody = null;

    @AfterAll
    public static void tearDown() {
        Speck.stop();

        beforeBody = null;
        routeBody = null;
        afterBody = null;
    }

    @BeforeAll
    public static void setup() {
        LOGGER.log(System.Logger.Level.DEBUG, "setup()");

        testUtil = new SpeckTestUtil(4567);

        beforeBody = null;
        routeBody = null;
        afterBody = null;

        before("/hello", (req, res) -> {
            LOGGER.log(System.Logger.Level.DEBUG, "before-req.body() = " + req.body());
            beforeBody = req.body();
        });

        post("/hello", (req, res) -> {
            LOGGER.log(System.Logger.Level.DEBUG, "get-req.body() = " + req.body());
            routeBody = req.body();
            return req.body();
        });

        after("/hello", (req, res) -> {
            LOGGER.log(System.Logger.Level.DEBUG, "after-before-req.body() = " + req.body());
            afterBody = req.body();
        });

        Speck.awaitInitialization();
    }

    @Test
    public void testPost() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("POST", "/hello", BODY_CONTENT);
        LOGGER.log(System.Logger.Level.INFO, response.body);
        assertEquals(HTTP_OK, response.status);
        assertTrue(response.body.contains(BODY_CONTENT));

        assertEquals(BODY_CONTENT, beforeBody);
        assertEquals(BODY_CONTENT, routeBody);
        assertEquals(BODY_CONTENT, afterBody);
    }
}
