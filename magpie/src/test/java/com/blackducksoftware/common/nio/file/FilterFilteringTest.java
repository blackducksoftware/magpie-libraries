/*
 * Copyright 2016 Black Duck Software, Inc.
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
package com.blackducksoftware.common.nio.file;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.common.test.FileSystemBuilder;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Tests that actually apply filters in a way that can be reproduced with {@code git ls-files} to verify correctness.
 *
 * @author jgustie
 */
public class FilterFilteringTest {

    @Test
    public void excludeFromTop_directory() throws Exception {
        try (FileSystem files = FileSystemBuilder.create().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            FileCollector result = new FileCollector();
            // Excludes "/bar", "/foo/bar", etc.
            filter(files).exclude("bar").walk(result);
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromTop_anchoredDirectory() throws Exception {
        try (FileSystem files = FileSystemBuilder.create().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            FileCollector result = new FileCollector();
            // Excludes "/bar", but not "/foo/bar"
            filter(files).exclude("/bar").walk(result);
            assertThat(result).isNotEmpty();
        }
    }

    @Test
    public void excludeFromTop_directoryOnly() throws Exception {
        try (FileSystem files = FileSystemBuilder.create().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            FileCollector result = new FileCollector();
            // Excludes "/foo/bar" because it is a directory
            filter(files).exclude("bar/").walk(result);
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromTop_doubleWild() throws Exception {
        try (FileSystem files = FileSystemBuilder.create().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            FileCollector result = new FileCollector();
            // Excludes "/bar/...", but not "/foo/bar/..." (pattern contains a separator!)
            filter(files).exclude("bar/**").walk(result);
            assertThat(result).isNotEmpty();
        }
    }

    @Test
    public void excludeFromTop_bookEndWild() throws Exception {
        try (FileSystem files = FileSystemBuilder.create().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            FileCollector result = new FileCollector();
            // Excludes "/foo/bar/gus"
            filter(files).exclude("*/bar/*").walk(result);
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromFile_excludesFromTop() throws Exception {
        try (FileSystem files = FileSystemBuilder.create()
                .addHelloWorld("/foo/bar/gus/test.txt")
                .addFile("/foo/excludeFrom", "/bar")
                .build()) {
            FileCollector result = new FileCollector();
            // Even though the file is in the foo directory, "/bar" matches from the root
            filter(files).excludeFrom(files.getPath("/foo/excludeFrom")).walk(result);
            assertThat(result).hasSize(2);
        }
    }

    @Test
    public void excludePerDirectory_excludesFromDir() throws Exception {
        try (FileSystem files = FileSystemBuilder.create()
                .addHelloWorld("/foo/bar/gus/test.txt")
                .addFile("/foo/excludePerDir", "/bar")
                .build()) {
            FileCollector result = new FileCollector();
            // Exclude per directory will exclude relative to the file itself
            filter(files).excludePerDirectory("excludePerDir").walk(result);
            assertThat(result).containsExactly("/foo/excludePerDir");
        }
    }

    @Test
    public void excludePerDirectory_mergeParentPatterns() throws Exception {
        try (FileSystem files = FileSystemBuilder.create()
                .addHelloWorld("/foo/bar/gus/test1.txt")
                .addHelloWorld("/foo/bar/gus/test2.txt")
                .addHelloWorld("/foo/bar/gus/test3.txt")
                .addHelloWorld("/foo/bar/gus/test4.txt")
                .addFile("/excludePerDir", "test1.txt")
                .addFile("/foo/excludePerDir", "test2.txt")
                .addFile("/foo/bar/excludePerDir", "test3.txt")
                .addFile("/foo/bar/gus/excludePerDir", "test4.txt")
                .build()) {
            FileCollector result = new FileCollector();
            // Exclude per directory files merge up to the top directory
            filter(files).exclude("excludePerDir").excludePerDirectory("excludePerDir").walk(result);
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludePerDirectory_negate() throws Exception {
        try (FileSystem files = FileSystemBuilder.create()
                .addHelloWorld("/foo/bar/gus/test1.txt")
                .addFile("/foo/excludePerDir", "test1.txt")
                .addFile("/foo/bar/gus/excludePerDir", "!test1.txt")
                .build()) {
            FileCollector result = new FileCollector();
            // Exclude per directory files merge up to the top directory
            filter(files).exclude("excludePerDir").excludePerDirectory("excludePerDir").walk(result);
            assertThat(result).containsExactly("/foo/bar/gus/test1.txt");
        }
    }

    @Test
    public void excludePerDirectory_excludeMasksNegate() throws Exception {
        try (FileSystem files = FileSystemBuilder.create()
                .addHelloWorld("/foo/bar/gus/test1.txt")
                .addFile("/foo/excludePerDir", "test1.txt")
                .addFile("/foo/bar/gus/excludePerDir", "!test1.txt")
                .build()) {
            FileCollector result = new FileCollector();
            // Same setup as the negate test, but ignore the file using an exclude
            filter(files).exclude("excludePerDir").excludePerDirectory("excludePerDir").exclude("*.txt").walk(result);
            assertThat(result).isEmpty();
        }
    }

    // -=-=-=-=-=-=- HELPERS -=-=-=-=-=-=-

    /**
     * Helper to create a filter from a file system's root directory.
     */
    private static Filter filter(FileSystem fileSystem) {
        return Filter.create(Iterables.getOnlyElement(fileSystem.getRootDirectories()));
    }

    /**
     * Collection for file paths that are encountered during a file tree walk operation. Note that while we could also
     * collect directory names, we are verifying this using a Git repository, and Git does not track directories.
     */
    private static class FileCollector extends SimpleFileVisitor<Path> implements Iterable<String> {
        private final List<Path> files = new LinkedList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            files.add(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public Iterator<String> iterator() {
            return Iterators.transform(files.iterator(), Functions.toStringFunction());
        }
    }

}
