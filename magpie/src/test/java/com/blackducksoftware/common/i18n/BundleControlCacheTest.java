/*
 * Copyright (C) 2015 Black Duck Software Inc.
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Tests for the {@code BundleControl} caching.
 *
 * @author jgustie
 */
public class BundleControlCacheTest {

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
