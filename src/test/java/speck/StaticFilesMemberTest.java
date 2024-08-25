/*
 * Copyright 2015 - Per Wendel
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.examples.exception.NotFoundException;
import speck.util.SpeckTestUtil;

import static speck.Speck.exception;
import static speck.Speck.get;
import static speck.Speck.staticFiles;

/**
 * Test static files
 */
public class StaticFilesMemberTest {

    private static final System.Logger LOGGER = System.getLogger(StaticFilesMemberTest.class.getName());;

    private static final String FO_SHIZZY = "Fo shizzy";
    private static final String NOT_FOUND_BRO = "Not found bro";

    private static final String EXTERNAL_FILE_NAME_HTML = "externalFile.html";

    private static final String CONTENT_OF_EXTERNAL_FILE = "Content of external file";

    private static SpeckTestUtil testUtil;

    private static File tmpExternalFile;

    @AfterAll
    public static void tearDown() {
        Speck.stop();
        if (tmpExternalFile != null) {
            LOGGER.log(System.Logger.Level.DEBUG, "tearDown().deleting: " + tmpExternalFile);
            tmpExternalFile.delete();
        }
    }

    @BeforeAll
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        tmpExternalFile = new File(System.getProperty("java.io.tmpdir"), EXTERNAL_FILE_NAME_HTML);

        FileWriter writer = new FileWriter(tmpExternalFile);
        writer.write(CONTENT_OF_EXTERNAL_FILE);
        writer.flush();
        writer.close();

        staticFiles.location("/public");
        staticFiles.externalLocation(System.getProperty("java.io.tmpdir"));

        get("/hello", (q, a) -> FO_SHIZZY);

        get("/*", (q, a) -> {
            throw new NotFoundException();
        });

        exception(NotFoundException.class, (e, request, response) -> {
            response.status(404);
            response.body(NOT_FOUND_BRO);
        });

        Speck.awaitInitialization();
    }

    @Test
    public void testStaticFileCssStyleCss() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/css/style.css", null);
        assertEquals(200, response.status);
        assertEquals("Content of css file", response.body);

        testGet();
    }

    @Test
    public void testStaticFileMjs() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/js/module.mjs", null);

        String expectedContentType = response.headers.firstValue("Content-Type").orElse(null);
        assertEquals(expectedContentType, "application/javascript");

        String body = response.body;
        assertEquals("export default function () { console.log(\"Hello, I'm a .mjs file\"); }\n", body);
    }

    @Test
    public void testStaticFilePagesIndexHtml() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/pages/index.html", null);
        assertEquals(200, response.status);
        assertEquals("<html><body>Hello Static World!</body></html>", response.body);

        testGet();
    }

    @Test
    public void testStaticFilePageHtml() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/page.html", null);
        assertEquals(200, response.status);
        assertEquals("<html><body>Hello Static Files World!</body></html>", response.body);

        testGet();
    }

    @Test
    public void testExternalStaticFile() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/externalFile.html", null);
        assertEquals(200, response.status);
        assertEquals("Content of external file", response.body);

        testGet();
    }

    @Test
    public void testStaticFileHeaders() throws Exception {
        staticFiles.headers(Map.of("Server", "Microsoft Word", "Cache-Control", "private, max-age=600"));
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/pages/index.html", null);
        assertEquals("Microsoft Word", response.headers.firstValue("Server").orElse(null));
        assertEquals("private, max-age=600", response.headers.firstValue("Cache-Control").orElse(null));

        testGet();
    }

    @Test
    public void testStaticFileExpireTime() throws Exception {
        staticFiles.expireTime(600);
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/pages/index.html", null);
        assertEquals("private, max-age=600", response.headers.firstValue("Cache-Control").orElse(null));

        testGet();
    }

    /**
     * Used to verify that "normal" functionality works after static files mapping
     */
    private static void testGet() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", "");

        assertEquals(200, response.status);
        assertTrue(response.body.contains(FO_SHIZZY));
    }

    @Test
    public void testExceptionMapping404() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/filethatdoesntexist.html", null);

        assertEquals(404, response.status);
        assertEquals(NOT_FOUND_BRO, response.body);
    }
}
