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

public class FilterTest {

    @Test
    public void defaultPatternNormalizer_blank() {
        // For various definitions of blank
        assertThat(Filter.defaultPatternNormalizer().apply("").collect(toList())).isEmpty();
        assertThat(Filter.defaultPatternNormalizer().apply(" ").collect(toList())).isEmpty();

        // Ignore a more general definition of whitespace
        assertThat(Filter.defaultPatternNormalizer().apply("\t").collect(toList())).isEmpty();
    }

    @Test
    public void defaultPatternNormalizer_comment() {
        // Comment
        assertThat(Filter.defaultPatternNormalizer().apply("# Test").collect(toList())).isEmpty();

        // Strict definition
        assertThat(Filter.defaultPatternNormalizer().apply(" # Not comment").collect(toList())).isNotEmpty();

        // Escaped
        assertThat(Filter.defaultPatternNormalizer().apply("\\# Test").collect(toList())).containsExactly("# Test");
    }

    @Test
    public void defaultPatternNormalizer_trailingSpace() {
        // Trailing spaces
        assertThat(Filter.defaultPatternNormalizer().apply("Foo ").collect(toList())).containsExactly("Foo");
        assertThat(Filter.defaultPatternNormalizer().apply("Foo  ").collect(toList())).containsExactly("Foo");

        // Not trailing whitespace
        assertThat(Filter.defaultPatternNormalizer().apply("Foo\t").collect(toList())).containsExactly("Foo\t");

        // Escaped trailing spaces
        assertThat(Filter.defaultPatternNormalizer().apply("Foo\\ ").collect(toList())).containsExactly("Foo ");
        assertThat(Filter.defaultPatternNormalizer().apply("Foo\\ \\ ").collect(toList())).containsExactly("Foo  ");

        // Both trailing and escaped
        assertThat(Filter.defaultPatternNormalizer().apply("Foo\\  ").collect(toList())).containsExactly("Foo ");
    }

}
