/*
 * Copyright 2018 Black Duck Software, Inc.
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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Extra path matcher helpers.
 *
 * @author jgustie
 */
public class ExtraPathMatchers {

    /**
     * Modifies a file visitor so that visits are filtered by the supplied path matcher.
     */
    public static FileVisitor<Path> filterVisitor(FileVisitor<Path> visitor, PathMatcher pathMatcher) {
        return new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(dir)) {
                    return visitor.preVisitDirectory(dir, attrs);
                } else {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(file)) {
                    return visitor.visitFile(file, attrs);
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return visitor.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return visitor.postVisitDirectory(dir, exc);
            }
        };
    }

    private ExtraPathMatchers() {
        assert false;
    }
}
