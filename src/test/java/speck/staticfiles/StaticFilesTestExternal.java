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
package speck.staticfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.Speck;
import speck.examples.exception.NotFoundException;
import speck.util.SpeckTestUtil;

import static speck.Speck.exception;
import static speck.Speck.get;
import static speck.Speck.staticFiles;

/**
 * Test external static files
 */
public class StaticFilesTestExternal {

    private static final System.Logger LOGGER = System.getLogger(StaticFilesTestExternal.class.getName());;

    private static final String FO_SHIZZY = "Fo shizzy";
    private static final String NOT_FOUND_BRO = "Not found bro";

    private static final String EXTERNAL_FILE_NAME_HTML = "externalFile.html";

    private static final String CONTENT_OF_EXTERNAL_FILE = "Content of external file";

    private static SpeckTestUtil testUtil;

    private static File tmpExternalFile1;
    private static File tmpExternalFile2;
    private static File folderOutsideStaticFiles;

    @AfterAll
    public static void tearDown() {
        Speck.stop();
        if (tmpExternalFile1 != null) {
            LOGGER.log(System.Logger.Level.DEBUG, "tearDown(). Deleting tmp files");
            tmpExternalFile1.delete();
            tmpExternalFile2.delete();
            folderOutsideStaticFiles.delete();
        }
    }

    @BeforeAll
    public static void setup() throws IOException {
        testUtil = new SpeckTestUtil(4567);

        String directoryRoot = System.getProperty("java.io.tmpdir") + "/speckish";
        new File(directoryRoot).mkdirs();

        tmpExternalFile1 = new File(directoryRoot, EXTERNAL_FILE_NAME_HTML);

        FileWriter writer = new FileWriter(tmpExternalFile1);
        writer.write(CONTENT_OF_EXTERNAL_FILE);
        writer.flush();
        writer.close();

        File root = new File(directoryRoot);

        folderOutsideStaticFiles = new File(root.getAbsolutePath() + "/../dumpsterstuff");
        folderOutsideStaticFiles.mkdirs();

        String newFilePath = root.getAbsolutePath() + "/../dumpsterstuff/Speck.class";
        tmpExternalFile2 = new File(newFilePath);
        tmpExternalFile2.createNewFile();

        staticFiles.externalLocation(directoryRoot);

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
    public void testExternalStaticFile() throws Exception {
        SpeckTestUtil.UrlResponse response = doGet("/externalFile.html");
        assertEquals(200, response.status);
        assertEquals("text/html", response.headers.firstValue("Content-Type").orElse(null));
        assertEquals(CONTENT_OF_EXTERNAL_FILE, response.body);

        testGet();
    }

    @Test
    public void testDirectoryTraversalProtectionExternal() throws Exception {
        String path = "/" + URLEncoder.encode("..\\..\\speck\\", StandardCharsets.UTF_8) + "Speck.class";
        SpeckTestUtil.UrlResponse response = doGet(path);

        assertEquals(404, response.status);
        assertEquals(NOT_FOUND_BRO, response.body);

        testGet();
    }

    private static void testGet() throws Exception {
        SpeckTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", "");

        assertEquals(200, response.status);
        assertTrue(response.body.contains(FO_SHIZZY));
    }

    private SpeckTestUtil.UrlResponse doGet(String fileName) throws Exception {
        return testUtil.doMethod("GET", fileName, null);
    }

}
