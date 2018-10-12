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

import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * Extra path matcher helpers.
 *
 * @author jgustie
 */
public class ExtraPathMatchers {

    /**
     * Modifies a file visitor so that visits are filtered by the supplied path matcher.
     *
     * @deprecated Use {@link ExtraFileVisitors#filter(FileVisitor, PathMatcher)} instead.
     */
    @Deprecated
    public static FileVisitor<Path> filterVisitor(FileVisitor<Path> visitor, PathMatcher pathMatcher) {
        return ExtraFileVisitors.filter(visitor, pathMatcher);
    }

    private ExtraPathMatchers() {
        assert false;
    }
}
