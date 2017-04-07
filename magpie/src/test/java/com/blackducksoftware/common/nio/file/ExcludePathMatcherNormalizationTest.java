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
import static java.util.stream.Collectors.toList;

import org.junit.Test;

/**
 * Tests for the {@code ExcludePathMatcher} default normalization of patterns.
 *
 * @author jgustie
 */
public class ExcludePathMatcherNormalizationTest {

    @Test
    public void defaultPatternNormalizer_blank() {
        // For various definitions of blank
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("").collect(toList())).isEmpty();
        assertThat(ExcludePathMatcher.defaultPatternNormalizer(" ").collect(toList())).isEmpty();

        // Ignore a more general definition of whitespace
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("\t").collect(toList())).isEmpty();
    }

    @Test
    public void defaultPatternNormalizer_comment() {
        // Comment
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("# Test").collect(toList())).isEmpty();

        // Strict definition
        assertThat(ExcludePathMatcher.defaultPatternNormalizer(" # Not comment").collect(toList())).isNotEmpty();

        // Escaped
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("\\# Test").collect(toList())).containsExactly("# Test");
    }

    @Test
    public void defaultPatternNormalizer_trailingSpace() {
        // Trailing spaces
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("Foo ").collect(toList())).containsExactly("Foo");
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("Foo  ").collect(toList())).containsExactly("Foo");

        // Not trailing whitespace
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("Foo\t").collect(toList())).containsExactly("Foo\t");

        // Escaped trailing spaces
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("Foo\\ ").collect(toList())).containsExactly("Foo ");
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("Foo\\ \\ ").collect(toList())).containsExactly("Foo  ");

        // Both trailing and escaped
        assertThat(ExcludePathMatcher.defaultPatternNormalizer("Foo\\  ").collect(toList())).containsExactly("Foo ");
    }

}
