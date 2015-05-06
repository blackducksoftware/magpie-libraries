/*
 * Copyright (C) 2014 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.common.i18n;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the {@code BundleControl}.
 * <p>
 * There are a few properties files that are required for this test (currently they are checked in,
 * but we could create them and load them via. a separate class loader if desired). These properties
 * files contain all of the characters from the data provider and are explicitly encoded using
 * character set they are named after (e.g. "UTF-8.properties" is "UTF-8" encoded). If a character
 * set does not support a particular character, the Unicode escape sequence must be used (the
 * {@code Properties} class handles these escapes for us); see ISO-8859-1 as an example.
 *
 * @author jgustie
 */
@RunWith(Parameterized.class)
public class BundleControlTest {

    /**
     * The default character set before this class starts running.
     */
    private static Charset defaultCharset;

    /**
     * Captures the default character set so we can change it for testing.
     */
    @BeforeClass
    public static void captureDefaultCharse() {
        defaultCharset = Charset.defaultCharset();
    }

    /**
     * Clears the default character set so the {@code file.encoding} property will be re-read.
     */
    @Before
    public void unsetDefaultCharset() throws ReflectiveOperationException {
        Field defaultCharset = Charset.class.getDeclaredField("defaultCharset");
        defaultCharset.setAccessible(true);
        defaultCharset.set(null, null);
    }

    /**
     * Restores the default character set observed before any of the tests ran.
     */
    @After
    public void restoreDefaultCharset() throws ReflectiveOperationException {
        System.setProperty("file.encoding", defaultCharset.name());
        unsetDefaultCharset();
        assert_()
        .withFailureMessage("unable to restore default charset")
        .that(Charset.defaultCharset())
        .isEqualTo(defaultCharset);
    }

    /**
     * Loads the specified bundle from the {@linkplain Locale#ROOT root locale} using the
     * {@code BundleControl} and the class loader of the test class.
     */
    protected ResourceBundle getBundle(String name) {
        return ResourceBundle.getBundle(name, Locale.ROOT, getClass().getClassLoader(), BundleControl.create());
    }

    // =============================================================================================

    /**
     * This is a very small subset of characters that are often encoded differently.
     */
    @Parameters
    public static List<Object[]> charsetData() {
        return Arrays.asList(new Object[][] {
                { "COPYRIGHT_SIGN", "\u00a9" },
                { "LEFT_DOUBLE_QUOTATION_MARK", "\u201c" },
                { "DAGGER", "\u2020" },
                { "CENT_SIGN", "\u00a2" },
                { "TILDE", "\u007e" }, });
    }

    private String key, value;

    public BundleControlTest(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Test
    public void testDefaultEncoding() {
        assertThat(getBundle(Charset.defaultCharset().name()).getString(key)).isEqualTo(value);
    }

    @Test
    public void testUtf8Encoding() {
        System.setProperty("file.encoding", "UTF-8");
        assertThat(getBundle("UTF-8").getString(key)).isEqualTo(value);
    }

    @Test
    public void testIso88591Encoding() {
        System.setProperty("file.encoding", "ISO-8859-1");
        assertThat(getBundle("ISO-8859-1").getString(key)).isEqualTo(value);
    }

    @Test
    public void testWindows1252Encoding() {
        System.setProperty("file.encoding", "windows-1252");
        assertThat(getBundle("windows-1252").getString(key)).isEqualTo(value);
    }

    @Test
    public void testCaching() throws IOException {
        final Path directory = Files.createTempDirectory(getClass().getName() + "#testCaching");
        try {
            final ClassLoader loader = new URLClassLoader(new URL[] { directory.toUri().toURL() });
            final Properties properties = new Properties();

            // Write out a properties file that contains 'test=foo'
            properties.setProperty("test", "foo");
            try (Writer out = new FileWriter(directory.resolve("test.properties").toFile())) {
                properties.store(out, null);
            }

            // Load the resource bundle, verify
            ResourceBundle bundle = ResourceBundle.getBundle("test", Locale.ROOT, loader, BundleControl.create());
            assertThat(bundle.getString("test")).isEqualTo("foo");

            // Overwrite the properties file with 'test=bar'
            properties.setProperty("test", "bar");
            try (Writer out = new FileWriter(directory.resolve("test.properties").toFile())) {
                properties.store(out, null);
            }

            // Load the resource bundle, verify the value is cached
            bundle = ResourceBundle.getBundle("test", Locale.ROOT, loader, BundleControl.create());
            assertThat(bundle.getString("test")).isEqualTo("foo");

            // Explicitly clear the cache
            ResourceBundle.clearCache(loader);

            // Load the resource bundle, verify the value is reloaded
            bundle = ResourceBundle.getBundle("test", Locale.ROOT, loader, BundleControl.create());
            assertThat(bundle.getString("test")).isEqualTo("bar");
        } finally {
            // Wipe out our temporary directory
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(directory, exc);
                }
            });
        }
    }
}
