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

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.System.arraycopy;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import speck.util.SpeckTestUtil;
import speck.util.SpeckTestUtil.UrlResponse;

/**

 I could not find an easy way to add a jar file to current classpath since Java9
 So I choose to ignore this test until we find a way

 */
@Disabled
public class StaticFilesFromArchiveTest {

    private static SpeckTestUtil testUtil;
    private static ClassLoader classLoader;
    private static ClassLoader initialClassLoader;

    @BeforeAll
    public static void setup() throws Exception {
        setupClassLoader();
        testUtil = new SpeckTestUtil(4567);

        Class<?> speckClass = classLoader.loadClass("speck.Speck");

        Method staticFileLocationMethod = speckClass.getMethod("staticFileLocation", String.class);
        staticFileLocationMethod.invoke(null, "/public-jar");

        Method initMethod = speckClass.getMethod("init");
        initMethod.invoke(null);

        Method awaitInitializationMethod = speckClass.getMethod("awaitInitialization");
        awaitInitializationMethod.invoke(null);
    }

    @AfterAll
    public static void resetClassLoader() {
        Thread.currentThread().setContextClassLoader(initialClassLoader);
    }

    private static void setupClassLoader() throws Exception {
        ClassLoader extendedClassLoader = createExtendedClassLoader();
        initialClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(extendedClassLoader);
        classLoader = extendedClassLoader;
    }

    private static URLClassLoader createExtendedClassLoader() {
        URL[] parentURLs = ((URLClassLoader) getSystemClassLoader()).getURLs();
        URL[] urls = new URL[parentURLs.length + 1];
        arraycopy(parentURLs, 0, urls, 0, parentURLs.length);

        URL publicJar = StaticFilesFromArchiveTest.class.getResource("/public-jar.zip");
        urls[urls.length - 1] = publicJar;

        // no parent classLoader because Speck and the static resources need to be loaded from the same classloader
        return new URLClassLoader(urls, null);
    }

    @Test
    @Disabled

    public void testCss() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/css/style.css", null);

        String expectedContentType = response.headers.firstValue("Content-Type").orElse(null);
        assertEquals(expectedContentType, "text/css");

        String body = response.body;
        assertEquals("Content of css file", body);
    }
}
