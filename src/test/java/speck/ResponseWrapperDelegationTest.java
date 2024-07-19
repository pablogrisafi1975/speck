package speck;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import speck.util.SpeckTestUtil;
import speck.util.SpeckTestUtil.UrlResponse;

import java.io.IOException;

import static speck.Speck.*;

public class ResponseWrapperDelegationTest {

    static SpeckTestUtil testUtil;

    @AfterClass
    public static void tearDown() {
        Speck.stop();
    }

    @BeforeClass
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        get("/204", (q, a) -> {
            a.status(204);
            return "";
        });

        after("/204", (q, a) -> {
            if (a.status() == 204) {
                a.status(200);
                a.body("ok");
            }
        });

        get("/json", (q, a) -> {
            a.type("application/json");
            return "{\"status\": \"ok\"}";
        });

        after("/json", (q, a) -> {
            if ("application/json".equalsIgnoreCase(a.type())) {
                a.type("text/plain");
            }
        });

        exception(Exception.class, (exception, q, a) -> exception.printStackTrace());

        Speck.awaitInitialization();
    }

    @Test
    public void filters_can_detect_response_status() throws Exception {
        UrlResponse response = testUtil.get("/204");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("ok", response.body);
    }

    @Test
    public void filters_can_detect_content_type() throws Exception {
        UrlResponse response = testUtil.get("/json");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("{\"status\": \"ok\"}", response.body);
        Assert.assertEquals("text/plain", response.headers.firstValue("Content-Type").orElse(null));
    }
}
