package speck;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.util.SpeckTestUtil;
import speck.util.SpeckTestUtil.UrlResponse;

import static speck.Speck.awaitInitialization;
import static speck.Speck.before;
import static speck.Speck.stop;

public class FilterTest {
    static SpeckTestUtil testUtil;

    @AfterAll
    public static void tearDown() {
        stop();
    }

    @BeforeAll
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        before("/justfilter", (q, a) -> System.out.println("Filter matched"));
        awaitInitialization();
    }

    @Test
    public void testJustFilter() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/justfilter", null);

        System.out.println("response.status = " + response.status);
        assertEquals(404, response.status);
    }

}
