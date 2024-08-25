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

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import speck.Speck;
import speck.examples.exception.NotFoundException;
import speck.util.SpeckTestUtil;

import static speck.Speck.get;
import static speck.Speck.staticFiles;

/**
 * Test static files
 */
public class DisableMimeGuessingTest {

    private static final System.Logger LOGGER = System.getLogger(StaticFilesTest.class.getName());;

    private static final String FO_SHIZZY = "Fo shizzy";
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
        staticFiles.disableMimeTypeGuessing();

        get("/hello", (q, a) -> FO_SHIZZY);

        get("/*", (q, a) -> {
            throw new NotFoundException();
        });

        Speck.awaitInitialization();
    }

    @Test
    public void testMimeTypes() throws Exception {
        assertNull(doGet("/pages/index.html").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/js/scripts.js").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/css/style.css").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/img/specklogo.png").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/img/specklogo.svg").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/img/specklogoPng").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/img/specklogoSvg").headers.firstValue("Content-Type").orElse(null));
        assertNull(doGet("/externalFile.html").headers.firstValue("Content-Type").orElse(null));
    }

    @Test
    public void testCustomMimeType() throws Exception {
        staticFiles.registerMimeType("cxt", "custom-extension-type");
        assertNull(doGet("/img/file.cxt").headers.firstValue("Content-Type").orElse(null));
    }

    private SpeckTestUtil.UrlResponse doGet(String fileName) throws Exception {
        return testUtil.doMethod("GET", fileName, null);
    }

}
