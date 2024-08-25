package speck;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import speck.util.SpeckTestUtil;
import speck.util.SpeckTestUtil.UrlResponse;

import java.util.HashMap;
import java.util.Map;

import static speck.Speck.after;
import static speck.Speck.before;
import static speck.Speck.get;
import static speck.Speck.halt;
import static speck.Speck.patch;
import static speck.Speck.post;


public class GenericSecureIntegrationTest {

    private static final System.Logger logger = System.getLogger(GenericSecureIntegrationTest.class.getName());
    static SpeckTestUtil testUtil;

    @AfterAll
    public static void tearDown() {
        Speck.stop();
    }

    @BeforeAll
    public static void setup() {
        testUtil = new SpeckTestUtil(4567);

        // note that the keystore stuff is retrieved from SpeckTestUtil which
        // respects JVM params for keystore, password
        // but offers a default included store if not.
        Speck.secure(SpeckTestUtil.getKeyStoreLocation(),
            SpeckTestUtil.getKeystorePassword(), null, null);

        before("/protected/*", (request, response) -> halt(401, "Go Away!"));

        get("/hi", (request, response) -> "Hello World!");

        get("/ip", (request, response) -> request.ip());

        get("/:param", (request, response) -> "echo: " + request.params(":param"));

        get("/paramwithmaj/:paramWithMaj", (request, response) -> "echo: " + request.params(":paramWithMaj"));

        get("/", (request, response) -> "Hello Root!");

        post("/poster", (request, response) -> {
            String body = request.body();
            response.status(201); // created
            return "Body was: " + body;
        });

        patch("/patcher", (request, response) -> {
            String body = request.body();
            response.status(200);
            return "Body was: " + body;
        });

        after("/hi", (request, response) -> response.header("after", "foobar"));

        Speck.awaitInitialization();
    }

    @Test
    public void testGetHi() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/hi", null);
        assertEquals(200, response.status);
        assertEquals("Hello World!", response.body);
    }

    @Test
    public void testXForwardedFor() throws Exception {
        final String xForwardedFor = "XXX.XXX.XXX.XXX";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-For", xForwardedFor);

        UrlResponse response = testUtil.doMethod("GET", "/ip", null, true, "text/html", headers);
        assertEquals(xForwardedFor, response.body);

        response = testUtil.doMethod("GET", "/ip", null, true, "text/html", null);
        assertNotEquals(xForwardedFor, response.body);
    }


    @Test
    public void testHiHead() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("HEAD", "/hi", null);
        assertEquals(200, response.status);
        assertEquals("", response.body);
    }

    @Test
    public void testGetHiAfterFilter() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/hi", null);
        assertTrue(response.headers.allValues("after").contains("foobar"));
    }

    @Test
    public void testGetRoot() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/", null);
        assertEquals(200, response.status);
        assertEquals("Hello Root!", response.body);
    }

    @Test
    public void testEchoParam1() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/shizzy", null);
        assertEquals(200, response.status);
        assertEquals("echo: shizzy", response.body);
    }

    @Test
    public void testEchoParam2() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/gunit", null);
        assertEquals(200, response.status);
        assertEquals("echo: gunit", response.body);
    }

    @Test
    public void testEchoParamWithMaj() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/paramwithmaj/plop", null);
        assertEquals(200, response.status);
        assertEquals("echo: plop", response.body);
    }

    @Test
    public void testUnauthorized() throws Exception {
        UrlResponse urlResponse = testUtil.doMethodSecure("GET", "/protected/resource", null);
        assertEquals(401, urlResponse.status);
    }

    @Test
    public void testNotFound() throws Exception {
        UrlResponse urlResponse = testUtil.doMethodSecure("GET", "/no/resource", null);
        assertEquals(404, urlResponse.status);
    }

    @Test
    public void testPost() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("POST", "/poster", "Fo shizzy");
        logger.log(System.Logger.Level.INFO, response.body);
        assertEquals(201, response.status);
        assertTrue(response.body.contains("Fo shizzy"));
    }

    @Test
    public void testPatch() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("PATCH", "/patcher", "Fo shizzy");
        logger.log(System.Logger.Level.INFO, response.body);
        assertEquals(200, response.status);
        assertTrue(response.body.contains("Fo shizzy"));
    }
}
