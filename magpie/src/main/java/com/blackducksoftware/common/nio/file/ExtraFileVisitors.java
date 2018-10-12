/*
 * Copyright 2018 Synopsys, Inc.
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

/**
 * Extra file visitor helpers.
 *
 * @author jgustie
 */
public class ExtraFileVisitors {

    /**
     * Simple file visitor that delegates to functional implementations. Unchecked I/O exceptions thrown by the supplied
     * functions are automatically unwrapped.
     */
    public static final class FunctionalFileVisitor<T> implements FileVisitor<T> {

        private final BiFunction<T, BasicFileAttributes, FileVisitResult> preVisitDirectory;

        private final BiFunction<T, BasicFileAttributes, FileVisitResult> visitFile;

        private final BiFunction<T, IOException, FileVisitResult> visitFileFailed;

        private final BiFunction<T, IOException, FileVisitResult> postVisitDirectory;

        public FunctionalFileVisitor(
                BiFunction<T, BasicFileAttributes, FileVisitResult> preVisitDirectory,
                BiFunction<T, BasicFileAttributes, FileVisitResult> visitFile,
                BiFunction<T, IOException, FileVisitResult> visitFileFailed,
                BiFunction<T, IOException, FileVisitResult> postVisitDirectory) {
            this.preVisitDirectory = Objects.requireNonNull(preVisitDirectory);
            this.visitFile = Objects.requireNonNull(visitFile);
            this.visitFileFailed = Objects.requireNonNull(visitFileFailed);
            this.postVisitDirectory = Objects.requireNonNull(postVisitDirectory);
        }

        public FunctionalFileVisitor(
                BiFunction<T, BasicFileAttributes, FileVisitResult> preVisitDirectory,
                BiFunction<T, BasicFileAttributes, FileVisitResult> visitFile) {
            this(preVisitDirectory, visitFile, FunctionalFileVisitor::rethrow, FunctionalFileVisitor::rethrow);
        }

        private static <P> FileVisitResult rethrow(P path, @Nullable IOException exc) {
            if (exc != null) {
                throw new UncheckedIOException(exc);
            } else {
                return FileVisitResult.CONTINUE;
            }
        }

        @Override
        public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
            try {
                return preVisitDirectory.apply(Objects.requireNonNull(dir), Objects.requireNonNull(attrs));
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        @Override
        public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
            try {
                return visitFile.apply(Objects.requireNonNull(file), Objects.requireNonNull(attrs));
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        @Override
        public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
            try {
                return visitFileFailed.apply(Objects.requireNonNull(file), Objects.requireNonNull(exc));
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }

        @Override
        public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
            try {
                return postVisitDirectory.apply(Objects.requireNonNull(dir), exc);
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
    }

    /**
     * A file visitor that simply delegates all method calls to another file visitor to simplify decoration.
     */
    public static abstract class ForwardingFileVisitor<T> implements FileVisitor<T> {

        private final FileVisitor<T> delegate;

        protected ForwardingFileVisitor(FileVisitor<T> delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException {
            return delegate.preVisitDirectory(dir, attrs);
        }

        @Override
        public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
            return delegate.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException {
            return delegate.visitFileFailed(file, exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException {
            return delegate.postVisitDirectory(dir, exc);
        }
    }

    /**
     * Modifies a file visitor so that visits are filtered by the supplied path matcher.
     */
    public static FileVisitor<Path> filter(FileVisitor<Path> visitor, PathMatcher matcher) {
        return new ForwardingFileVisitor<Path>(visitor) {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return matcher.matches(dir) ? super.preVisitDirectory(dir, attrs) : FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                return matcher.matches(file) ? super.visitFile(file, attrs) : FileVisitResult.CONTINUE;
            }
        };
    }

    private ExtraFileVisitors() {
        assert false;
    }
}
