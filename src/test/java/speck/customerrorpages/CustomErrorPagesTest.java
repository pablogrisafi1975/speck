package speck.customerrorpages;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.CustomErrorPages;
import speck.Speck;
import speck.util.SpeckTestUtil;

import static speck.Speck.get;
import static speck.Speck.internalServerError;
import static speck.Speck.notFound;

public class CustomErrorPagesTest {

    private static final String CUSTOM_NOT_FOUND = "custom not found 404";
    private static final String CUSTOM_INTERNAL = "custom internal 500";
    private static final String HELLO_WORLD = "hello world!";
    public static final String APPLICATION_JSON = "application/json";
    private static final String QUERY_PARAM_KEY = "qparkey";

    static SpeckTestUtil testUtil;

    @AfterAll
    public static void tearDown() {
        Speck.stop();
    }

    @BeforeAll
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        get("/hello", (q, a) -> HELLO_WORLD);

        get("/raiseinternal", (q, a) -> {
            throw new Exception("");
        });

        notFound(CUSTOM_NOT_FOUND);

        internalServerError((request, response) -> {
            if (request.queryParams(QUERY_PARAM_KEY) != null) {
                throw new Exception();
            }
            response.type(APPLICATION_JSON);
            return CUSTOM_INTERNAL;
        });

        Speck.awaitInitialization();
    }

    @Test
    public void testGetHi() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null);
        assertEquals(200, response.status);
        assertEquals(HELLO_WORLD, response.body);
    }

    @Test
    public void testCustomNotFound() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/othernotmapped", null);
        assertEquals(404, response.status);
        assertEquals(CUSTOM_NOT_FOUND, response.body);
    }

    @Test
    public void testCustomInternal() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/raiseinternal", null);
        assertEquals(500, response.status);
        assertEquals(APPLICATION_JSON, response.headers.firstValue("Content-Type").orElse(null));
        assertEquals(CUSTOM_INTERNAL, response.body);
    }

    @Test
    public void testCustomInternalFailingRoute() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/raiseinternal?" + QUERY_PARAM_KEY + "=sumthin", null);
        assertEquals(500, response.status);
        assertEquals(CustomErrorPages.INTERNAL_ERROR, response.body);
    }

}
