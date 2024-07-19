package speck;

import org.junit.Assert;
import org.junit.Test;

import speck.util.SpeckTestUtil;

import static speck.Speck.awaitInitialization;
import static speck.Speck.get;
import static speck.Speck.unmap;

public class UnmapTest {

    SpeckTestUtil testUtil = new SpeckTestUtil(4567);

    @Test
    public void testUnmap() throws Exception {
        get("/tobeunmapped", (q, a) -> "tobeunmapped");
        awaitInitialization();

        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/tobeunmapped", null);
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("tobeunmapped", response.body);

        unmap("/tobeunmapped");

        response = testUtil.doMethod("GET", "/tobeunmapped", null);
        Assert.assertEquals(404, response.status);

        get("/tobeunmapped", (q, a) -> "tobeunmapped");

        response = testUtil.doMethod("GET", "/tobeunmapped", null);
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("tobeunmapped", response.body);

        unmap("/tobeunmapped", "get");

        response = testUtil.doMethod("GET", "/tobeunmapped", null);
        Assert.assertEquals(404, response.status);
    }
}
