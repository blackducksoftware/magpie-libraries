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

import org.junit.Test;

/**
 * Tests for {@code ExtraStrings}.
 *
 * @author jgustie
 */
public class ExtraStringsTest {

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

}