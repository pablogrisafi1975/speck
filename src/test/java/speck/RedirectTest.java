/*
 * Copyright 2016 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package speck;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.util.SpeckTestUtil;

import static speck.Speck.get;
import static speck.Speck.redirect;

/**
 * Tests the redirect utility methods in {@link speck.Redirect}
 */
public class RedirectTest {

    private static final String REDIRECTED = "Redirected";

    private static SpeckTestUtil testUtil;

    @BeforeAll
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);
        testUtil.setFollowRedirectStrategy(301, 302); // don't set the others to be able to verify affect of Redirect.Status

        get("/hello", (request, response) -> REDIRECTED);

        redirect.get("/hi", "/hello");
        redirect.post("/hi", "/hello");
        redirect.put("/hi", "/hello");
        redirect.delete("/hi", "/hello");
        redirect.any("/any", "/hello");

        redirect.get("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.post("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.put("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.delete("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.any("/anyagain", "/hello", Redirect.Status.USE_PROXY);

        Speck.awaitInitialization();
    }

    @Test
    public void testRedirectGet() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectPost() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("POST", "/hi", "");
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectPut() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/hi", "");
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectDelete() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/hi", null);
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectAnyGet() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/any", null);
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectAnyPut() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/any", "");
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectAnyPost() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("POST", "/any", "");
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectAnyDelete() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/any", "");
        assertEquals(200, response.status);
        assertEquals(REDIRECTED, response.body);
    }

    @Test
    public void testRedirectGetWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hiagain", null);
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectPostWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("POST", "/hiagain", "");
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectPutWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/hiagain", "");
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectDeleteWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/hiagain", null);
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectAnyGetWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/anyagain", null);
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectAnyPostWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("POST", "/anyagain", "");
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectAnyPutWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/anyagain", "");
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

    @Test
    public void testRedirectAnyDeleteWithSpecificCode() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/anyagain", null);
        assertEquals(Redirect.Status.USE_PROXY.intValue(), response.status);
    }

}
