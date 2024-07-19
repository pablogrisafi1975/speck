package speck;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import speck.util.SpeckTestUtil;

import static speck.Speck.after;
import static speck.Speck.awaitInitialization;
import static speck.Speck.before;
import static speck.Speck.get;
import static speck.Speck.stop;

/**
 * Basic test to ensure that multiple before and after filters can be mapped to a route.
 */
public class MultipleFiltersTest {
    
    private static SpeckTestUtil http;


    @BeforeClass
    public static void setup() {
        http = new SpeckTestUtil(4567);

        before("/user", initializeCounter, incrementCounter, loadUser);

        after("/user", incrementCounter, (req, res) -> {
            int counter = req.attribute("counter");
            Assert.assertEquals(counter, 2);
        });

        get("/user", (request, response) -> {
            Assert.assertEquals((int) request.attribute("counter"), 1);
            return ((User) request.attribute("user")).name();
        });

        awaitInitialization();
    }

    @AfterClass
    public static void stopServer() {
        stop();
    }

    @Test
    public void testMultipleFilters() {
        try {
            SpeckTestUtil.UrlResponse response = http.get("/user");
            Assert.assertEquals(200, response.status);
            Assert.assertEquals("Kevin", response.body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Filter loadUser = (request, response) -> {
        User u = new User();
        u.name("Kevin");
        request.attribute("user", u);
    };

    private static Filter initializeCounter = (request, response) -> request.attribute("counter", 0);

    private static Filter incrementCounter = (request, response) -> {
        int counter = request.attribute("counter");
        counter++;
        request.attribute("counter", counter);
    };

    private static class User {

        private String name;

        public String name() {
            return name;
        }

        public void name(String name) {
            this.name = name;
        }
    }
}
