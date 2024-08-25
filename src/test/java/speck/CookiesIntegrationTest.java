package speck;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static speck.Speck.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * System tests for the Cookies support.
 *
 * @author dreambrother
 */
public class CookiesIntegrationTest {

    private static final String DEFAULT_HOST_URL = "http://localhost:4567";
    private HttpClient httpClient = HttpClientBuilder.create().build();

    @BeforeAll
    public static void initRoutes() throws InterruptedException {
        post("/assertNoCookies", (request, response) -> {
            if (!request.cookies().isEmpty()) {
                halt(500);
            }
            return "";
        });

        post("/setCookie", (request, response) -> {
            response.cookie(request.queryParams("cookieName"), request.queryParams("cookieValue"));
            return "";
        });

        post("/assertHasCookie", (request, response) -> {
            String cookieValue = request.cookie(request.queryParams("cookieName"));
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                halt(500);
            }
            return "";
        });

        post("/removeCookie", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.cookie(cookieName);
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                halt(500);
            }
            response.removeCookie(cookieName);
            return "";
        });

        post("/path/setCookieWithPath", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.queryParams("cookieValue");
            response.cookie("/path", cookieName, cookieValue, -1, false);
            return "";
        }) ;

        post("/path/removeCookieWithPath", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.cookie(cookieName);
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                halt(500);
            }
            response.removeCookie("/path", cookieName);
            return "";
        }) ;

        post("/path/assertNoCookies", (request, response) -> {
            if (!request.cookies().isEmpty()) {
                halt(500);
            }
            return "";
        });

    }

    @AfterAll
    public static void stopServer() {
        Speck.stop();
    }

    @Test
    public void testEmptyCookies() {
        httpPost("/assertNoCookies");
    }

    @Test
    public void testCreateCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertHasCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
    }

    @Test
    public void testRemoveCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/removeCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertNoCookies");
    }

    @Test
    public void testRemoveCookieWithPath() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/path/setCookieWithPath?cookieName=" + cookieName + "&cookieValue=" + cookieValue);

        // for sanity, check that cookie is not sent with request if path doesn't match
        httpPost("/assertNoCookies");

        // now remove cookie with matching path
        httpPost("/path/removeCookieWithPath?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/path/assertNoCookies");
    }

    private void httpPost(String relativePath) {
        HttpPost request = new HttpPost(DEFAULT_HOST_URL + relativePath);
        try {
            HttpResponse response = httpClient.execute(request);
            assertEquals(200, response.getStatusLine().getStatusCode());
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            request.releaseConnection();
        }
    }
}
