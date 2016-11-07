/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
