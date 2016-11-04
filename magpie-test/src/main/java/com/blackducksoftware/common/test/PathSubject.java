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
package com.blackducksoftware.common.test;

import java.nio.file.Path;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.Truth;

/**
 * Stand in until Truth has path support. Because a {@code Path} is both an {@code Iterable<Path>} and a
 * {@code Comparable<Path>} it generates an ambiguity on the overloaded {@code Truth.assertThat} method.
 *
 * @author jgustie
 * @see <a href="https://github.com/google/truth/issues/172">Issue #172</a>
 */
public class PathSubject extends Subject<PathSubject, Path> {

    private static final SubjectFactory<PathSubject, Path> FACTORY = new SubjectFactory<PathSubject, Path>() {
        @Override
        public PathSubject getSubject(FailureStrategy fs, Path that) {
            return new PathSubject(fs, that);
        }
    };

    /**
     * Temporary method for static importing that works around the ambiguity of {@code Path}.
     * <p>
     * This will be removed when Truth is fixed.
     */
    public static PathSubject assertThatPath(Path target) {
        return Truth.assertAbout(FACTORY).that(target);
    }

    /**
     * Temporary method for static importing that works around the ambiguity of {@code Path}.
     * <p>
     * This will be removed when Truth is fixed.
     */
    public static SubjectFactory<PathSubject, Path> paths() {
        return FACTORY;
    }

    private PathSubject(FailureStrategy failureStrategy, Path subject) {
        super(failureStrategy, subject);
    }

}
