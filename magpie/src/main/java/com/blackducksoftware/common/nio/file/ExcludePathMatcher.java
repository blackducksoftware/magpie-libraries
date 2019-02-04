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

import static com.blackducksoftware.common.base.ExtraStrings.ensureDelimiter;
import static com.blackducksoftware.common.nio.file.FnMatch.fnmatch;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

/**
 * A filtering mechanism based on Git conventions.
 *
 * @author jgustie
 */
public class ExcludePathMatcher implements PathMatcher {

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
            this.pattern = Objects.requireNonNull(pattern);
            this.directory = Objects.requireNonNull(directory);
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
            boolean pathnameMatch = pattern.indexOf('/') >= 0;

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
            return Boolean.compare(other.negate, negate);
        }

        @Override
        public final boolean matches(Path path) {
            // Relativize the path against the proper directory
            Path relativePath = directory.relativize(path);

            // Match the full path name or the name segments
            boolean matches = false;
            if (pathnameMatch) {
                matches = fnmatch(pattern, ensureDelimiter(relativePath, "/"), PATHNAME);
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
     * The list of files names to read per-directory exclusions from.
     */
    private final List<String> excludePerDirectoryNames;

    /**
     * The list of path matchers for this filter.
     */
    private final List<PatternPathMatcher> matchers;

    /**
     * A mapping of lazily computed per-directory path matchers.
     */
    private final Map<Path, List<PatternPathMatcher>> perDirectoryMatchers = new ConcurrentHashMap<>();

    private ExcludePathMatcher(Builder builder) {
        top = Objects.requireNonNull(builder.top);
        patternNormalizer = Objects.requireNonNull(builder.patternNormalizer);
        excludePerDirectoryNames = ImmutableList.copyOf(builder.excludePerDirectoryNames);

        // Previously we respected the addition order between patterns and files, this would
        // require reading file patterns in the builder if we were to bring that behavior back
        matchers = Stream.concat(builder.patterns.stream(), builder.files.stream().flatMap(ExcludePathMatcher::readAllLines))
                .flatMap(patternNormalizer)
                .map(pattern -> PatternPathMatcher.create(pattern, top))
                .sorted()
                .collect(toImmutableList());
    }

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
            for (PatternPathMatcher matcher : perDirectoryMatchers.computeIfAbsent(path.getParent(), this::directoryMatchers)) {
                if (matcher.matches(path)) {
                    return matcher.negate;
                }
            }
        }

        return true;
    }

    /**
     * Computes the list of pattern path matchers based on the exclude-per-directory rules.
     */
    private List<PatternPathMatcher> directoryMatchers(Path directory) {
        return readDirectoryPatterns(top, excludePerDirectoryNames, directory)
                .flatMap(patternNormalizer)
                .map(pattern -> PatternPathMatcher.create(pattern, directory))
                .sorted()
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    /**
     * Recursively read all of the patterns from all of the exclude-per-directory files.
     */
    private static Stream<String> readDirectoryPatterns(Path top, List<String> excludePerDirectoryNames, Path directory) {
        Stream<String> patterns = excludePerDirectoryNames.stream()
                .map(directory::resolve)
                .filter(Files::exists)
                .flatMap(ExcludePathMatcher::readAllLines);

        // Recursively load parent patterns up to the root
        Path parent = directory.getParent();
        if (parent != null && parent.startsWith(top)) {
            return Stream.concat(patterns, readDirectoryPatterns(top, excludePerDirectoryNames, parent));
        } else {
            return patterns;
        }
    }

    /**
     * Reads all of the lines from a file using the system default character set.
     */
    private static Stream<String> readAllLines(Path file) {
        try {
            return Files.readAllLines(file, Charset.defaultCharset()).stream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Used to normalize a sequence of patterns. Comments and blank lines are stripped and trailing spaces are trimmed.
     * This method will apply escapes, e.g. if this function returns a pattern that starts with a "#" or has trailing
     * spaces, it is because they were escaped in the input.
     */
    // TODO Consider something like Iterator<String> so we are not returning a stream in the public API
    public static Stream<String> defaultPatternNormalizer(String line) {
        // Skip blank lines and comments
        if (line == null || CharMatcher.whitespace().matchesAllOf(line) || line.charAt(0) == '#') {
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
        while (trailingSpace-- > 0) {
            pattern += " ";
        }

        return Stream.of(pattern);
    }

    public static class Builder {

        private Path top;

        private Function<String, Stream<String>> patternNormalizer;

        private final List<String> patterns = new ArrayList<>();

        private final List<Path> files = new ArrayList<>();

        private final List<String> excludePerDirectoryNames = new ArrayList<>();

        public Builder() {
            top = Paths.get(System.getProperty("user.dir"));
            patternNormalizer = ExcludePathMatcher::defaultPatternNormalizer;
        }

        public Builder from(Path top) {
            this.top = Objects.requireNonNull(top);
            return this;
        }

        // TODO Consider Function<String, Iterable|Iterator<String>> so we are not forcing stream as a return type
        public Builder normalizingPatterns(Function<String, Stream<String>> patternNormalizer) {
            this.patternNormalizer = Objects.requireNonNull(patternNormalizer);
            return this;
        }

        public Builder exclude(String pattern) {
            patterns.add(Objects.requireNonNull(pattern));
            return this;
        }

        public Builder excludeFrom(Path file) {
            files.add(Objects.requireNonNull(file));
            return this;
        }

        public Builder excludePerDirectory(String name) {
            excludePerDirectoryNames.add(Objects.requireNonNull(name));
            return this;
        }

        /**
         * Creates a new exclusion rule based path matcher.
         */
        public ExcludePathMatcher build() {
            return new ExcludePathMatcher(this);
        }
    }
}
