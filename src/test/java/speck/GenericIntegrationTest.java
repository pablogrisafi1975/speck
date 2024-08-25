package speck;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import speck.examples.exception.BaseException;
import speck.examples.exception.JWGmeligMeylingException;
import speck.examples.exception.NotFoundException;
import speck.examples.exception.SubclassOfBaseException;
import speck.util.SpeckTestUtil;
import speck.util.SpeckTestUtil.UrlResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static speck.Speck.after;
import static speck.Speck.afterAfter;
import static speck.Speck.before;
import static speck.Speck.exception;
import static speck.Speck.externalStaticFileLocation;
import static speck.Speck.get;
import static speck.Speck.halt;
import static speck.Speck.patch;
import static speck.Speck.path;
import static speck.Speck.post;
import static speck.Speck.staticFileLocation;


public class GenericIntegrationTest {

    private static final String NOT_FOUND_BRO = "Not found bro";

    private static final System.Logger logger = System.getLogger(GenericIntegrationTest.class.getName());
    ;

    static SpeckTestUtil testUtil;
    static File tmpExternalFile;

    @AfterAll
    public static void tearDown() {
        Speck.stop();
        if (tmpExternalFile != null) {
            tmpExternalFile.delete();
        }
    }

    @BeforeAll
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        tmpExternalFile = new File(System.getProperty("java.io.tmpdir"), "externalFile.html");

        FileWriter writer = new FileWriter(tmpExternalFile);
        writer.write("Content of external file");
        writer.flush();
        writer.close();

        staticFileLocation("/public");
        externalStaticFileLocation(System.getProperty("java.io.tmpdir"));


        before("/secretcontent/*", (q, a) -> {
            halt(401, "Go Away!");
        });

        before("/protected/*", "application/xml", (q, a) -> {
            halt(401, "Go Away!");
        });

        before("/protected/*", "application/json", (q, a) -> {
            halt(401, "{\"message\": \"Go Away!\"}");
        });

        get("/hi", "application/json", (q, a) -> "{\"message\": \"Hello World\"}");
        get("/hi", (q, a) -> "Hello World!");
        get("/binaryhi", (q, a) -> "Hello World!".getBytes());
        get("/bytebufferhi", (q, a) -> ByteBuffer.wrap("Hello World!".getBytes()));
        get("/inputstreamhi", (q, a) -> new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8)));
        get("/param/:param", (q, a) -> "echo: " + q.params(":param"));

        path("/firstPath", () -> {
            before("/*", (q, a) -> a.header("before-filter-ran", "true"));
            get("/test", (q, a) -> "Single path-prefix works");
            path("/secondPath", () -> {
                get("/test", (q, a) -> "Nested path-prefix works");
                path("/thirdPath", () -> {
                    get("/test", (q, a) -> "Very nested path-prefix works");
                });
            });
        });

        get("/paramandwild/:param/stuff/*", (q, a) -> "paramandwild: " + q.params(":param") + q.splat()[0]);
        get("/paramwithmaj/:paramWithMaj", (q, a) -> "echo: " + q.params(":paramWithMaj"));

        get("/templateView", (q, a) -> new ModelAndView("Hello", "my view"), new TemplateEngine() {
            @Override
            public String render(ModelAndView modelAndView) {
                return modelAndView.getModel() + " from " + modelAndView.getViewName();
            }
        });

        get("/", (q, a) -> "Hello Root!");

        post("/poster", (q, a) -> {
            String body = q.body();
            a.status(201); // created
            return "Body was: " + body;
        });

        post("/post_via_get", (q, a) -> {
            a.status(201); // created
            return "Method Override Worked";
        });

        get("/post_via_get", (q, a) -> "Method Override Did Not Work");

        patch("/patcher", (q, a) -> {
            String body = q.body();
            a.status(200);
            return "Body was: " + body;
        });
/*
        get("/session_reset", (q, a) -> {
            String key = "session_reset";
            Session session = q.session();
            session.attribute(key, "11111");
            session.invalidate();
            session = q.session();
            session.attribute(key, "22222");
            return session.attribute(key);
        });
*/
        get("/ip", (request, response) -> request.ip());

        after("/hi", (q, a) -> {

            if (q.requestMethod().equalsIgnoreCase("get")) {
                assertNotNull(a.body());
            }

            a.header("after", "foobar");
        });

        get("/throwexception", (q, a) -> {
            throw new UnsupportedOperationException();
        });

        get("/throwsubclassofbaseexception", (q, a) -> {
            throw new SubclassOfBaseException();
        });

        get("/thrownotfound", (q, a) -> {
            throw new NotFoundException();
        });

        get("/throwmeyling", (q, a) -> {
            throw new JWGmeligMeylingException();
        });

        exception(JWGmeligMeylingException.class, (meylingException, q, a) -> {
            a.body(meylingException.trustButVerify());
        });

        exception(UnsupportedOperationException.class, (exception, q, a) -> {
            a.body("Exception handled");
        });

        exception(BaseException.class, (exception, q, a) -> {
            a.body("Exception handled");
        });

        exception(NotFoundException.class, (exception, q, a) -> {
            a.status(404);
            a.body(NOT_FOUND_BRO);
        });

        get("/exception", (request, response) -> {
            throw new RuntimeException();
        });

        afterAfter("/exception", (request, response) -> {
            response.body("done executed for exception");
        });

        post("/nice", (request, response) -> "nice response");

        afterAfter("/nice", (request, response) -> {
            response.header("post-process", "nice done response");
        });

        afterAfter((request, response) -> {
            response.header("post-process-all", "nice done response after all");
        });

        Speck.awaitInitialization();
    }

    private static void registerEchoRoute(final String routePart) {
        get("/tworoutes/" + routePart + "/:param", (q, a) -> {
            return routePart + " route: " + q.params(":param");
        });
    }

    private static void assertEchoRoute(String routePart) throws Exception {
        final String expected = "expected";
        UrlResponse response = testUtil.doMethod("GET", "/tworoutes/" + routePart + "/" + expected, null);
        assertEquals(200, response.status);
        assertEquals(routePart + " route: " + expected, response.body);
    }

    @Test
    public void filters_should_be_accept_type_aware() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/protected/resource", null, "application/json");
        assertEquals(401, response.status);
        assertEquals("{\"message\": \"Go Away!\"}", response.body);
    }

    @Test
    public void routes_should_be_accept_type_aware() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/hi", null, "application/json");
        assertEquals(200, response.status);
        assertEquals("{\"message\": \"Hello World\"}", response.body);
    }

    @Test
    public void template_view_should_be_rendered_with_given_model_view_object() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/templateView", null);
        assertEquals(200, response.status);
        assertEquals("Hello from my view", response.body);
    }

    @Test
    public void testGetHi() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertEquals(200, response.status);
        assertEquals("Hello World!", response.body);
    }

    @Test
    public void testGetBinaryHi() {
        try {
            UrlResponse response = testUtil.doMethod("GET", "/binaryhi", null);
            assertEquals(200, response.status);
            assertEquals("Hello World!", response.body);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetByteBufferHi() {
        try {
            UrlResponse response = testUtil.doMethod("GET", "/bytebufferhi", null);
            assertEquals(200, response.status);
            assertEquals("Hello World!", response.body);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetInputStreamHi() {
        try {
            UrlResponse response = testUtil.doMethod("GET", "/inputstreamhi", null);
            assertEquals(200, response.status);
            assertEquals("Hello World!", response.body);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHiHead() throws Exception {
        UrlResponse response = testUtil.doMethod("HEAD", "/hi", null);
        assertEquals(200, response.status);
        assertEquals("", response.body);
    }

    @Test
    public void testGetHiAfterFilter() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertTrue(response.headers.allValues("after").contains("foobar"));
    }

    @Test
    public void testXForwardedFor() throws Exception {
        final String xForwardedFor = "XXX.XXX.XXX.XXX";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-For", xForwardedFor);

        UrlResponse response = testUtil.doMethod("GET", "/ip", null, false, "text/html", headers);
        assertEquals(xForwardedFor, response.body);

        response = testUtil.doMethod("GET", "/ip", null, false, "text/html", null);
        assertNotEquals(xForwardedFor, response.body);
    }

    @Test
    public void testGetRoot() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/", null);
        assertEquals(200, response.status);
        assertEquals("Hello Root!", response.body);
    }

    @Test
    public void testParamAndWild() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/paramandwild/thedude/stuff/andits", null);
        assertEquals(200, response.status);
        assertEquals("paramandwild: thedudeandits", response.body);
    }

    @Test
    public void testEchoParam1() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/param/shizzy", null);
        assertEquals(200, response.status);
        assertEquals("echo: shizzy", response.body);
    }

    @Test
    public void testEchoParam2() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/param/gunit", null);
        assertEquals(200, response.status);
        assertEquals("echo: gunit", response.body);
    }

    @Test
    public void testEchoParam3() throws Exception {
        String polyglot = "жξ Ä 聊";
        String encoded = new URI(null, null, polyglot, null).toASCIIString();
        UrlResponse response = testUtil.doMethod("GET", "/param/" + encoded, null);
        assertEquals(200, response.status);
        assertEquals("echo: " + polyglot, response.body);
    }

    @Test
    public void testPathParamsWithPlusSign() throws Exception {
        String pathParamWithPlusSign = "not+broken+path+param";
        UrlResponse response = testUtil.doMethod("GET", "/param/" + pathParamWithPlusSign, null);
        assertEquals(200, response.status);
        assertEquals("echo: " + pathParamWithPlusSign, response.body);
    }

    @Test
    public void testParamWithEncodedSlash() throws Exception {
        String polyglot = "te/st";
        String encoded = URLEncoder.encode(polyglot, StandardCharsets.UTF_8);
        UrlResponse response = testUtil.doMethod("GET", "/param/" + encoded, null);
        assertEquals(200, response.status);
        assertEquals("echo: " + polyglot, response.body);
    }

    @Test
    public void testSplatWithEncodedSlash() throws Exception {
        String param = "fo/shizzle";
        String encodedParam = URLEncoder.encode(param, StandardCharsets.UTF_8);
        String splat = "mah/FRIEND";
        String encodedSplat = URLEncoder.encode(splat, StandardCharsets.UTF_8);
        UrlResponse response = testUtil.doMethod("GET",
            "/paramandwild/" + encodedParam + "/stuff/" + encodedSplat, null);
        assertEquals(200, response.status);
        assertEquals("paramandwild: " + param + splat, response.body);
    }

    @Test
    public void testEchoParamWithUpperCaseInValue() throws Exception {
        final String camelCased = "ThisIsAValueAndSpeckShouldRetainItsUpperCasedCharacters";
        UrlResponse response = testUtil.doMethod("GET", "/param/" + camelCased, null);
        assertEquals(200, response.status);
        assertEquals("echo: " + camelCased, response.body);
    }

    @Test
    public void testTwoRoutesWithDifferentCaseButSameName() throws Exception {
        String lowerCasedRoutePart = "param";
        String upperCasedRoutePart = "PARAM";

        registerEchoRoute(lowerCasedRoutePart);
        registerEchoRoute(upperCasedRoutePart);
        assertEchoRoute(lowerCasedRoutePart);
        assertEchoRoute(upperCasedRoutePart);
    }

    @Test
    public void testEchoParamWithMaj() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/paramwithmaj/plop", null);
        assertEquals(200, response.status);
        assertEquals("echo: plop", response.body);
    }

    @Test
    public void testUnauthorized() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/secretcontent/whateva", null);
        assertEquals(401, response.status);
    }

    @Test
    public void testNotFound() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/no/resource", null);
        assertEquals(404, response.status);
    }

    @Test
    public void testPost() throws Exception {
        UrlResponse response = testUtil.doMethod("POST", "/poster", "Fo shizzy");
        logger.log(System.Logger.Level.INFO, response.body);
        assertEquals(201, response.status);
        assertTrue(response.body.contains("Fo shizzy"));
    }

    @Test
    public void testPostViaGetWithMethodOverrideHeader() throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("X-HTTP-Method-Override", "POST");
        UrlResponse response = testUtil.doMethod("GET", "/post_via_get", "Fo shizzy", false, "*/*", map);
        System.out.println(response.body);
        assertEquals(201, response.status);
        assertTrue(response.body.contains("Method Override Worked"));
    }

    @Test
    public void testPatch() throws Exception {
        UrlResponse response = testUtil.doMethod("PATCH", "/patcher", "Fo shizzy");
        logger.log(System.Logger.Level.INFO, response.body);
        assertEquals(200, response.status);
        assertTrue(response.body.contains("Fo shizzy"));
    }

    @Test
    public void testStaticFile() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/css/style.css", null);
        assertEquals(200, response.status);
        assertEquals("Content of css file", response.body);
    }

    @Test
    public void testExternalStaticFile() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/externalFile.html", null);
        assertEquals(200, response.status);
        assertEquals("Content of external file", response.body);
    }

    @Test
    public void testExceptionMapper() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/throwexception", null);
        assertEquals("Exception handled", response.body);
    }

    @Test
    public void testInheritanceExceptionMapper() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/throwsubclassofbaseexception", null);
        assertEquals("Exception handled", response.body);
    }

    @Test
    public void testNotFoundExceptionMapper() throws Exception {
        //        thrownotfound
        UrlResponse response = testUtil.doMethod("GET", "/thrownotfound", null);
        assertEquals(NOT_FOUND_BRO, response.body);
        assertEquals(404, response.status);
    }

    @Test
    public void testTypedExceptionMapper() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/throwmeyling", null);
        assertEquals(new JWGmeligMeylingException().trustButVerify(), response.body);
    }


    @Test
    public void path_should_prefix_routes() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/firstPath/test", null, "application/json");
        assertEquals(200, response.status);
        assertEquals("Single path-prefix works", response.body);
        assertEquals("true", response.headers.firstValue("before-filter-ran").orElse(null));
    }

    @Test
    public void paths_should_be_nestable() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/firstPath/secondPath/test", null, "application/json");
        assertEquals(200, response.status);
        assertEquals("Nested path-prefix works", response.body);
        assertEquals("true", response.headers.firstValue("before-filter-ran").orElse(null));
    }

    @Test
    public void paths_should_be_very_nestable() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/firstPath/secondPath/thirdPath/test", null, "application/json");
        assertEquals(200, response.status);
        assertEquals("Very nested path-prefix works", response.body);
        assertEquals("true", response.headers.firstValue("before-filter-ran").orElse(null));
    }

    @Test
    public void testRuntimeExceptionForDone() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/exception", null);
        assertEquals("done executed for exception", response.body);
        assertEquals(500, response.status);
    }

    @Test
    public void testRuntimeExceptionForAllRoutesFinally() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertEquals("foobar", response.headers.firstValue("after").orElse(null));
        assertEquals("nice done response after all", response.headers.firstValue("post-process-all").orElse(null));
        assertEquals(200, response.status);
    }

    @Test
    public void testPostProcessBodyForFinally() throws Exception {
        UrlResponse response = testUtil.doMethod("POST", "/nice", "");
        assertEquals("nice response", response.body);
        assertEquals("nice done response", response.headers.firstValue("post-process").orElse(null));
        assertEquals(200, response.status);
    }


}
