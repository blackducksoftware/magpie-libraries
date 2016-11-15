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

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
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
            ClassLoader loader = new URLClassLoader(new URL[] { directory.toUri().toURL() });
            Properties properties = new Properties();

            // Write out a properties file that contains 'test=foo'
            properties.setProperty("test", "foo");
            try (Writer out = Files.newBufferedWriter(directory.resolve("test.properties"), Charset.defaultCharset())) {
                properties.store(out, null);
            }

            // Load the resource bundle, verify
            ResourceBundle bundle = ResourceBundle.getBundle("test", Locale.ROOT, loader, BundleControl.create());
            assertThat(bundle.getString("test")).isEqualTo("foo");

            // Overwrite the properties file with 'test=bar'
            properties.setProperty("test", "bar");
            try (Writer out = Files.newBufferedWriter(directory.resolve("test.properties"), Charset.defaultCharset())) {
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
