package speck;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import speck.util.SpeckTestUtil;
import speck.util.SpeckTestUtil.UrlResponse;

import static speck.Speck.awaitInitialization;
import static speck.Speck.before;
import static speck.Speck.stop;

public class FilterTest {
    static SpeckTestUtil testUtil;

    @AfterClass
    public static void tearDown() {
        stop();
    }

    @BeforeClass
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        before("/justfilter", (q, a) -> System.out.println("Filter matched"));
        awaitInitialization();
    }

    @Test
    public void testJustFilter() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/justfilter", null);

        System.out.println("response.status = " + response.status);
        Assert.assertEquals(404, response.status);
    }

}
