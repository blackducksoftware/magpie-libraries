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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
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
public class ExcludePathMatcherFilteringTest {

    @Test
    public void excludeFromTop_directory() throws Exception {
        try (FileSystem files = new FileSystemBuilder().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            // Excludes "/bar", "/foo/bar", etc.
            FileCollector result = walkFileTree(files, filter(files).exclude("bar").build());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromTop_anchoredDirectory() throws Exception {
        try (FileSystem files = new FileSystemBuilder().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            // Excludes "/bar", but not "/foo/bar"
            FileCollector result = walkFileTree(files, filter(files).exclude("/bar").build());
            assertThat(result).isNotEmpty();
        }
    }

    @Test
    public void excludeFromTop_directoryOnly() throws Exception {
        try (FileSystem files = new FileSystemBuilder().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            // Excludes "/foo/bar" because it is a directory
            FileCollector result = walkFileTree(files, filter(files).exclude("bar/").build());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromTop_doubleWild() throws Exception {
        try (FileSystem files = new FileSystemBuilder().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            // Excludes "/bar/...", but not "/foo/bar/..." (pattern contains a separator!)
            FileCollector result = walkFileTree(files, filter(files).exclude("bar/**").build());
            assertThat(result).isNotEmpty();
        }
    }

    @Test
    public void excludeFromTop_bookEndWild() throws Exception {
        try (FileSystem files = new FileSystemBuilder().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            // Excludes "/foo/bar/gus"
            FileCollector result = walkFileTree(files, filter(files).exclude("*/bar/*").build());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromFile_excludesFromTop() throws Exception {
        try (FileSystem files = new FileSystemBuilder()
                .addHelloWorld("/foo/bar/gus/test.txt")
                .addFile("/foo/excludeFrom", "/bar")
                .build()) {
            // Even though the file is in the foo directory, "/bar" matches from the root
            FileCollector result = walkFileTree(files, filter(files).excludeFrom(files.getPath("/foo/excludeFrom")).build());
            assertThat(result).hasSize(2);
        }
    }

    @Test
    public void excludePerDirectory_excludesFromDir() throws Exception {
        try (FileSystem files = new FileSystemBuilder()
                .addHelloWorld("/foo/bar/gus/test.txt")
                .addFile("/foo/excludePerDir", "/bar")
                .build()) {
            // Exclude per directory will exclude relative to the file itself
            FileCollector result = walkFileTree(files, filter(files).excludePerDirectory("excludePerDir").build());
            assertThat(result).containsExactly("/foo/excludePerDir");
        }
    }

    @Test
    public void excludePerDirectory_mergeParentPatterns() throws Exception {
        try (FileSystem files = new FileSystemBuilder()
                .addHelloWorld("/foo/bar/gus/test1.txt")
                .addHelloWorld("/foo/bar/gus/test2.txt")
                .addHelloWorld("/foo/bar/gus/test3.txt")
                .addHelloWorld("/foo/bar/gus/test4.txt")
                .addFile("/excludePerDir", "test1.txt")
                .addFile("/foo/excludePerDir", "test2.txt")
                .addFile("/foo/bar/excludePerDir", "test3.txt")
                .addFile("/foo/bar/gus/excludePerDir", "test4.txt")
                .build()) {
            // Exclude per directory files merge up to the top directory
            FileCollector result = walkFileTree(files, filter(files).exclude("excludePerDir").excludePerDirectory("excludePerDir").build());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludePerDirectory_negate() throws Exception {
        try (FileSystem files = new FileSystemBuilder()
                .addHelloWorld("/foo/bar/gus/test1.txt")
                .addFile("/foo/excludePerDir", "test1.txt")
                .addFile("/foo/bar/gus/excludePerDir", "!test1.txt")
                .build()) {
            // Exclude per directory files merge up to the top directory
            FileCollector result = walkFileTree(files, filter(files).exclude("excludePerDir").excludePerDirectory("excludePerDir").build());
            assertThat(result).containsExactly("/foo/bar/gus/test1.txt");
        }
    }

    @Test
    public void excludePerDirectory_excludeMasksNegate() throws Exception {
        try (FileSystem files = new FileSystemBuilder()
                .addHelloWorld("/foo/bar/gus/test1.txt")
                .addFile("/foo/excludePerDir", "test1.txt")
                .addFile("/foo/bar/gus/excludePerDir", "!test1.txt")
                .build()) {
            // Same setup as the negate test, but ignore the file using an exclude
            FileCollector result = walkFileTree(files, filter(files).exclude("excludePerDir").excludePerDirectory("excludePerDir").exclude("*.txt").build());
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void excludeFromTop_anchoredDirectoryWindows() throws Exception {
        try (FileSystem files = new FileSystemBuilder().asWindows().addHelloWorld("/foo/bar/gus/test.txt").build()) {
            // Exclusion pattern contains "/" which isn't a windows separator
            FileCollector result = walkFileTree(files, filter(files).exclude("/foo/bar").build());
            assertThat(result).isEmpty();
        }
    }

    // -=-=-=-=-=-=- HELPERS -=-=-=-=-=-=-

    /**
     * Helper to create a filter from a file system's root directory.
     */
    private static ExcludePathMatcher.Builder filter(FileSystem fileSystem) {
        return new ExcludePathMatcher.Builder().from(Iterables.getOnlyElement(fileSystem.getRootDirectories()));
    }

    /**
     * Helper to walk a file tree filtered using the supplied path matcher.
     */
    private static FileCollector walkFileTree(FileSystem fileSystem, PathMatcher pathMatcher) throws IOException {
        FileCollector result = new FileCollector();
        Path start = Iterables.getOnlyElement(fileSystem.getRootDirectories());
        Files.walkFileTree(start, ExtraPathMatchers.filterVisitor(result, pathMatcher));
        return result;
    }

    /**
     * Collection for file paths that are encountered during a file tree walk operation. Note that while we could also
     * collect directory names, we are verifying this using a Git repository, and Git does not track directories.
     */
    private static class FileCollector extends SimpleFileVisitor<Path> implements Iterable<String> {
        private final List<Path> files = new ArrayList<>();

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
