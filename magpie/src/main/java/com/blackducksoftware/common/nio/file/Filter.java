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

import static com.blackducksoftware.common.nio.file.FnMatch.fnmatch;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;

/**
 * A filtering mechanism based on Git conventions.
 *
 * @author jgustie
 */
public class Filter {

    /**
     * A path matcher for an individual pattern.
     */
    private static class PatternPathMatcher implements PathMatcher, Comparable<PatternPathMatcher> {

        private static final EnumSet<FnMatch.Flag> PATHNAME = EnumSet.of(FnMatch.Flag.PATHNAME);

        private final String pattern;

        private final Path directory;

        private final boolean negate;

        private final boolean directoriesOnly;

        private final boolean pathnameMatch;

        private PatternPathMatcher(String pattern, Path directory, boolean negate, boolean directoriesOnly, boolean pathnameMatch) {
            this.pattern = checkNotNull(pattern);
            this.directory = checkNotNull(directory);
            this.negate = negate;
            this.directoriesOnly = directoriesOnly;
            this.pathnameMatch = pathnameMatch;
        }

        /**
         * Creates a new path matcher instance for the specified raw pattern.
         */
        private static PatternPathMatcher create(String rawPattern, Path directory) {
            String pattern = rawPattern;
            boolean negate = false;
            boolean directoryOnly = false;

            // Negate
            if (pattern.charAt(0) == '!') {
                negate = true;
                pattern = pattern.substring(1);
            }
            if (pattern.startsWith("\\!")) {
                pattern = pattern.substring(1);
            }

            // Directory
            if (pattern.charAt(pattern.length() - 1) == '/') {
                directoryOnly = true;
                pattern = pattern.substring(0, pattern.length() - 1);
            }

            // Pathname match
            boolean pathnameMatch = CharMatcher.is('/').matchesAnyOf(pattern);

            // Anchored match is covered since we keep track of the directory
            if (pattern.charAt(0) == '/') {
                pattern = pattern.substring(1);
            }

            return new PatternPathMatcher(pattern, directory, negate, directoryOnly, pathnameMatch);
        }

        @Override
        public int compareTo(PatternPathMatcher other) {
            // We want to leverage the stable sorting and only consider the negated state
            // since negated matchers need to be evaluated first
            return ComparisonChain.start().compareTrueFirst(negate, other.negate).result();
        }

        @Override
        public final boolean matches(Path path) {
            // Relativize the path against the proper directory
            Path relativePath = directory.relativize(path);

            // Match the full path name or the name segments
            boolean matches = false;
            if (pathnameMatch) {
                matches = fnmatch(pattern, relativePath.toString(), PATHNAME);
            } else {
                for (int i = 0; i < relativePath.getNameCount() && !matches; ++i) {
                    matches = fnmatch(pattern, relativePath.getName(i).toString());
                }
            }

            // Restrict to directories if necessary
            return matches && (!directoriesOnly || Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS));
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("PatternPathMatcher{'").append(pattern)
                    .append("', negate=").append(negate)
                    .append(", dir=").append(directoriesOnly)
                    .append(", pn=").append(pathnameMatch)
                    .append("}").toString();
        }
    }

    /**
     * A path matcher used for evaluating the enclosing filter instance. We only need to create one of these per
     * {@code Filter} instance.
     */
    private class FilterPathMatcher implements PathMatcher {
        @Override
        public boolean matches(Path path) {
            if (!path.startsWith(top)) {
                // We can only match within the top directory
                return false;
            } else if (path.equals(top)) {
                // The top must match
                return true;
            }

            // If a top level matcher matches, the path is excluded
            for (PatternPathMatcher matcher : matchers) {
                if (matcher.matches(path)) {
                    return matcher.negate;
                }
            }

            // If an ignore file is present evaluate that
            if (!excludePerDirectoryNames.isEmpty()) {
                for (PatternPathMatcher matcher : perDirectoryMatchers.getUnchecked(path.getParent())) {
                    if (matcher.matches(path)) {
                        return matcher.negate;
                    }
                }
            }

            return true;
        }
    }

    /**
     * A visitor that applies the enclosing filter prior to delegating visitor calls.
     */
    private final class FilteredFileVisitor implements FileVisitor<Path> {
        private final FileVisitor<Path> delegate;

        private FilteredFileVisitor(FileVisitor<Path> delegate) {
            this.delegate = checkNotNull(delegate);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (pathMatcher.matches(dir)) {
                return delegate.preVisitDirectory(dir, attrs);
            } else {
                return FileVisitResult.SKIP_SUBTREE;
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (pathMatcher.matches(file)) {
                return delegate.visitFile(file, attrs);
            } else {
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return delegate.visitFileFailed(file, exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return delegate.postVisitDirectory(dir, exc);
        }
    }

    /**
     * The top directory this filter matches against. This is how paths are relativized, so the filter will not be able
     * to match paths outside this hierarchy.
     */
    private final Path top;

    /**
     * The normalizer for processing raw patterns supplied by the user (either through exclude files or command line
     * options). If the pattern is meaningless, this function will map to an empty iterable.
     */
    private final Function<String, Stream<String>> patternNormalizer;

    /**
     * The list of path matchers for this filter.
     */
    private final List<PatternPathMatcher> matchers = new LinkedList<>();

    /**
     * The list of files names to read per-directory exclusions from.
     */
    private final List<String> excludePerDirectoryNames = new LinkedList<>();

    /**
     * A mapping of lazily computed per-directory path matchers.
     */
    private final LoadingCache<Path, List<PatternPathMatcher>> perDirectoryMatchers = CacheBuilder.newBuilder()
            .build(new CacheLoader<Path, List<PatternPathMatcher>>() {
                @Override
                public List<PatternPathMatcher> load(final Path directory) {
                    // Turn patterns into path matchers
                    return patterns(directory)
                            .flatMap(patternNormalizer)
                            .map(pattern -> PatternPathMatcher.create(pattern, directory))
                            .sorted()
                            .collect(collectingAndThen(toList(), Collections::unmodifiableList));
                }

                private Stream<String> patterns(Path directory) {
                    // Read all of the patterns from all of the existing exclude-per-directory files
                    Stream<String> patterns = excludePerDirectoryNames.stream()
                            .map(directory::resolve)
                            .filter(Files::exists)
                            .flatMap(file -> {
                                try {
                                    return Files.readAllLines(file, Charset.defaultCharset()).stream();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });

                    // Recursively load parent patterns up to the root
                    Path parent = directory.getParent();
                    if (parent != null && parent.startsWith(top)) {
                        return Stream.concat(patterns, patterns(parent));
                    } else {
                        return patterns;
                    }
                }
            });

    /**
     * The path matcher backed by this filter.
     */
    private final FilterPathMatcher pathMatcher = new FilterPathMatcher();

    private Filter(Path top, Function<String, Stream<String>> patternNormalizer) {
        this.top = checkNotNull(top);
        this.patternNormalizer = checkNotNull(patternNormalizer);
        checkArgument(Files.isDirectory(top), "top must be a directory");
    }

    /**
     * Creates a new filter for matching paths within the specified top level directory.
     */
    public static Filter create(Path top) {
        return create(top, defaultPatternNormalizer());
    }

    /**
     * Creates a new filter for matching paths within the specified top level directory.
     */
    public static Filter create(Path top, Function<String, Stream<String>> patternNormalizer) {
        return new Filter(top, patternNormalizer);
    }

    /**
     * Excludes files matching the specified pattern.
     */
    public final Filter exclude(String pattern) {
        return addMatches(Stream.of(pattern));
    }

    /**
     * Read exclude patterns from the specified file.
     */
    public final Filter excludeFrom(Path file) throws IOException {
        return addMatches(Files.readAllLines(file, Charset.defaultCharset()).stream());
    }

    /**
     * Add the normalized pattern matches.
     */
    private Filter addMatches(Stream<String> patterns) {
        patterns.flatMap(patternNormalizer)
                .map(pattern -> PatternPathMatcher.create(pattern, top))
                .forEach(matchers::add);
        Collections.sort(matchers);
        return this;
    }

    /**
     * Read additional exclude patterns that only apply to supplied file's parent and sub-directories.
     */
    public final Filter excludePerDirectory(String name) {
        excludePerDirectoryNames.add(name);
        perDirectoryMatchers.invalidateAll();
        return this;
    }

    /**
     * Returns this filter as a path matcher. Changes to the filter are reflected in the resulting path matcher.
     */
    public PathMatcher asPathMatcher() {
        return pathMatcher;
    }

    /**
     * Walks the file tree from the root directory of this filter. This filter is applied during the walk so excluded
     * files will not be seen by the supplied visitor.
     */
    public Path walk(FileVisitor<Path> visitor) throws IOException {
        return Files.walkFileTree(top, new FilteredFileVisitor(visitor));
    }

    /**
     * Used to normalize a sequence of patterns. Comments and blank lines are stripped and trailing spaces are trimmed.
     * This method will apply escapes, e.g. if this function returns a pattern that starts with a "#" or has trailing
     * spaces, it is because they were escaped in the input.
     */
    public static Function<String, Stream<String>> defaultPatternNormalizer() {
        return line -> {
            // Skip blank lines and comments
            if (line == null || CharMatcher.WHITESPACE.matchesAllOf(line) || line.charAt(0) == '#') {
                return Stream.empty();
            }

            // Unescape comments
            String pattern = line;
            if (pattern.startsWith("\\#")) {
                pattern = pattern.substring(1);
            }

            // Remove unescaped trailing space
            while (pattern.endsWith(" ") && !pattern.endsWith("\\ ")) {
                pattern = pattern.substring(0, pattern.length() - 1);
            }

            // Unescape trailing space
            int trailingSpace = 0;
            while (pattern.endsWith("\\ ")) {
                pattern = pattern.substring(0, pattern.length() - 2);
                trailingSpace++;
            }
            if (trailingSpace > 0) {
                pattern += Strings.repeat(" ", trailingSpace);
            }

            return Stream.of(pattern);
        };
    }

}
