/*
 * Copyright 2015 Black Duck Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackducksoftware.common.i18n;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@code BundleControl} caching.
 *
 * @author jgustie
 */
public class BundleControlCacheTest {

    private Path bundleFile;

    @Before
    public void createTemporaryPropertyBundleFile() throws IOException {
        bundleFile = Files.createTempFile(getClass().getSimpleName() + "#testCaching", ".properties");
    }

    @After
    public void deleteTemporaryPropertyBundleFile() throws IOException {
        Files.deleteIfExists(bundleFile);
    }

    @Test
    public void testCaching() throws IOException {
        ClassLoader loader = loader();
        BundleControl control = BundleControl.create();

        writeUTF8Properties(bundleFile, "test", "foo");
        assertThat(ResourceBundle.getBundle(baseName(), targetLocale(), loader, control).getString("test")).isEqualTo("foo");

        writeUTF8Properties(bundleFile, "test", "bar");
        assertThat(ResourceBundle.getBundle(baseName(), targetLocale(), loader, control).getString("test")).isEqualTo("foo");

        ResourceBundle.clearCache(loader);
        assertThat(ResourceBundle.getBundle(baseName(), targetLocale(), loader, control).getString("test")).isEqualTo("bar");
    }

    @Test
    public void testDevelopmentControl() throws IOException {
        ClassLoader loader = loader();
        BundleControl control = BundleControl.createDevelopmentControl();

        writeUTF8Properties(bundleFile, "test", "foo");
        assertThat(ResourceBundle.getBundle(baseName(), targetLocale(), loader, control).getString("test")).isEqualTo("foo");

        writeUTF8Properties(bundleFile, "test", "bar");
        assertThat(ResourceBundle.getBundle(baseName(), targetLocale(), loader, control).getString("test")).isEqualTo("bar");
    }

    /**
     * Returns the bundle base name given the current temporary bundle file.
     */
    private String baseName() {
        String result = bundleFile.getFileName().toString();
        return result.substring(0, result.length() - ".properties".length());
    }

    /**
     * Returns the target locale given the current temporary bundle file.
     */
    private Locale targetLocale() {
        return Locale.ROOT;
    }

    /**
     * Returns the class loader given the current temporary bundle file.
     */
    private ClassLoader loader() throws MalformedURLException {
        return new URLClassLoader(new URL[] { bundleFile.getParent().toUri().toURL() });
    }

    /**
     * Writes a UTF-8 encoded properties file to the specified path, overwriting any existing properties.
     */
    private static void writeUTF8Properties(Path path, String... nameValuePairs) throws IOException {
        checkArgument(nameValuePairs.length % 2 == 0, "expected even number of entries for creating bundle");
        Properties properties = new Properties();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            properties.setProperty(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        try (Writer out = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(out, null);
        }
    }

}
