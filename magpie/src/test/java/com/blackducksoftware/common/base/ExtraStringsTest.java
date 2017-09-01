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
package com.blackducksoftware.common.base;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import org.junit.Test;

/**
 * Tests for {@code ExtraStrings}.
 *
 * @author jgustie
 */
public class ExtraStringsTest {

    @Test
    public void ofEmptyNullValue() {
        assertThat(ExtraStrings.ofEmpty(null)).isEmpty();
    }

    @Test
    public void ofEmptyEmptyValue() {
        assertThat(ExtraStrings.ofEmpty("")).isEmpty();
    }

    @Test
    public void ofEmptyValue() {
        assertThat(ExtraStrings.ofEmpty("foo")).isPresent();
    }

    @Test
    public void ofBlankNullValue() {
        assertThat(ExtraStrings.ofBlank(null)).isEmpty();
    }

    @Test
    public void ofBlankEmptyValue() {
        assertThat(ExtraStrings.ofBlank("")).isEmpty();
    }

    @Test
    public void ofBlankBlankValue() {
        assertThat(ExtraStrings.ofBlank(" ")).isEmpty();
        assertThat(ExtraStrings.ofBlank("  ")).isEmpty();
        assertThat(ExtraStrings.ofBlank("\t")).isEmpty();
        assertThat(ExtraStrings.ofBlank("\r")).isEmpty();
        assertThat(ExtraStrings.ofBlank("\n")).isEmpty();
    }

    @Test
    public void ofBlankValue() {
        assertThat(ExtraStrings.ofBlank("foo")).isPresent();
        assertThat(ExtraStrings.ofBlank(" foo")).isPresent();
        assertThat(ExtraStrings.ofBlank(" foo ")).isPresent();
    }

    @Test(expected = NullPointerException.class)
    public void ensurePrefixNullPrefix() {
        ExtraStrings.ensurePrefix(null, "");
    }

    @Test
    public void ensurePrefixNullValue() {
        assertThat(ExtraStrings.ensurePrefix("x", null)).isNull();
    }

    @Test
    public void ensurePrefixEmptyPrefix() {
        assertThat(ExtraStrings.ensurePrefix("", "x")).isEqualTo("x");
    }

    @Test
    public void ensurePrefixEmptyValue() {
        assertThat(ExtraStrings.ensurePrefix("x", "")).isEqualTo("x");
    }

    @Test
    public void ensurePrefixEmptyPrefixAndValue() {
        assertThat(ExtraStrings.ensurePrefix("", "")).isEmpty();
    }

    @Test
    public void ensurePrefixWithoutPrefix() {
        assertThat(ExtraStrings.ensurePrefix("x", "y")).isEqualTo("xy");
    }

    @Test
    public void ensurePrefixWithPrefix() {
        assertThat(ExtraStrings.ensurePrefix("x", "xy")).isEqualTo("xy");
    }

    @Test
    public void ensurePrefixWithoutMultiCharPrefix() {
        assertThat(ExtraStrings.ensurePrefix("xy", "z")).isEqualTo("xyz");
    }

    @Test
    public void ensurePrefixWithMultiCharPrefix() {
        assertThat(ExtraStrings.ensurePrefix("xy", "xyz")).isEqualTo("xyz");
    }

    @Test(expected = NullPointerException.class)
    public void ensureDelimiterNullDelimiter() {
        ExtraStrings.ensureDelimiter("", null, "");
    }

    @Test
    public void ensureDelimiterNullStart() {
        assertThat(ExtraStrings.ensureDelimiter(null, "x", "y")).isEqualTo("xy");
    }

    @Test
    public void ensureDelimiterNullEnd() {
        assertThat(ExtraStrings.ensureDelimiter("x", "y", null)).isEqualTo("xy");
    }

    @Test
    public void ensureDelimiterNullStartAndEnd() {
        assertThat(ExtraStrings.ensureDelimiter(null, "x", null)).isEqualTo("x");
    }

    @Test
    public void ensureDelimiterWithoutStartOrEndDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("x", "y", "z")).isEqualTo("xyz");
    }

    @Test
    public void ensureDelimiterWithStartDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("xy", "y", "z")).isEqualTo("xyz");
    }

    @Test
    public void ensureDelimiterWithEndDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("x", "y", "yz")).isEqualTo("xyz");
    }

    @Test
    public void ensureDelimiterWithStartAndEndDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("xy", "y", "yz")).isEqualTo("xyz");
    }

    @Test
    public void ensureDelimiterWithoutStartOrEndMultiCharDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("w", "xy", "z")).isEqualTo("wxyz");
    }

    @Test
    public void ensureDelimiterWithStartMultiCharDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("wxy", "xy", "z")).isEqualTo("wxyz");
    }

    @Test
    public void ensureDelimiterWithEndMultiCharDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("w", "xy", "xyz")).isEqualTo("wxyz");
    }

    @Test
    public void ensureDelimiterWithStartAndEndMultiCharDelimiter() {
        assertThat(ExtraStrings.ensureDelimiter("wxy", "xy", "xyz")).isEqualTo("wxyz");
    }

    @Test
    public void beforeLastMatching() {
        assertThat(ExtraStrings.beforeLast("wxyz", 'y')).isEqualTo("wx");
    }

    @Test
    public void beforeLastNotMatching() {
        assertThat(ExtraStrings.beforeLast("wxyz", 'a')).isEqualTo("wxyz");
    }

    @Test
    public void beforeLastNull() {
        assertThat(ExtraStrings.beforeLast(null, 'a')).isNull();
    }

    @Test
    public void afterLastMatching() {
        assertThat(ExtraStrings.afterLast("wxyz", 'y')).isEqualTo("z");
    }

    @Test
    public void afterLastNotMatching() {
        assertThat(ExtraStrings.afterLast("wxyz", 'a')).isEqualTo("wxyz");
    }

    @Test
    public void afterLastNull() {
        assertThat(ExtraStrings.afterLast(null, 'a')).isNull();
    }

}
