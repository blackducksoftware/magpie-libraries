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

import java.util.Objects;
import java.util.StringJoiner;

import org.junit.Test;

/**
 * Tests for {@code ExtraStrings}.
 *
 * @author jgustie
 */
public class ExtraStringsTest {

    @Test(expected = NullPointerException.class)
    public void isNotEmptyNullValue() {
        ExtraStrings.isNotEmpty(null);
    }

    @Test
    public void isNotEmptyEmptyValue() {
        assertThat(ExtraStrings.isNotEmpty("")).isFalse();
    }

    @Test
    public void isNotEmptyValue() {
        assertThat(ExtraStrings.isNotEmpty("foo")).isTrue();
    }

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
    public void ensureSuffixNullSuffix() {
        ExtraStrings.ensureSuffix("", null);
    }

    @Test
    public void ensureSuffixNullValue() {
        assertThat(ExtraStrings.ensureSuffix(null, "x")).isNull();
    }

    @Test
    public void ensureSuffixEmptySuffix() {
        assertThat(ExtraStrings.ensureSuffix("x", "")).isEqualTo("x");
    }

    @Test
    public void ensureSuffixEmptyValue() {
        assertThat(ExtraStrings.ensureSuffix("", "x")).isEqualTo("x");
    }

    @Test
    public void ensureSuffixEmptySuffixAndValue() {
        assertThat(ExtraStrings.ensureSuffix("", "")).isEmpty();
    }

    @Test
    public void ensureSuffixWithoutSuffix() {
        assertThat(ExtraStrings.ensureSuffix("x", "y")).isEqualTo("xy");
    }

    @Test
    public void ensureSuffixWithSuffix() {
        assertThat(ExtraStrings.ensureSuffix("xy", "y")).isEqualTo("xy");
    }

    @Test
    public void ensureSuffixWithoutMultiCharSuffix() {
        assertThat(ExtraStrings.ensureSuffix("x", "yz")).isEqualTo("xyz");
    }

    @Test
    public void ensureSuffixWithMultiCharSuffix() {
        assertThat(ExtraStrings.ensureSuffix("xyz", "yz")).isEqualTo("xyz");
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
    public void padBothOdd() {
        assertThat(ExtraStrings.padBoth("x", 0, ' ')).isEqualTo("x");
        assertThat(ExtraStrings.padBoth("x", 1, ' ')).isEqualTo("x");
        assertThat(ExtraStrings.padBoth("x", 2, ' ')).isEqualTo("x ");
        assertThat(ExtraStrings.padBoth("x", 3, ' ')).isEqualTo(" x ");
        assertThat(ExtraStrings.padBoth("x", 4, ' ')).isEqualTo(" x  ");
        assertThat(ExtraStrings.padBoth("x", 5, ' ')).isEqualTo("  x  ");
    }

    @Test
    public void padBothEven() {
        assertThat(ExtraStrings.padBoth("xx", 1, ' ')).isEqualTo("xx");
        assertThat(ExtraStrings.padBoth("xx", 2, ' ')).isEqualTo("xx");
        assertThat(ExtraStrings.padBoth("xx", 3, ' ')).isEqualTo("xx ");
        assertThat(ExtraStrings.padBoth("xx", 4, ' ')).isEqualTo(" xx ");
        assertThat(ExtraStrings.padBoth("xx", 5, ' ')).isEqualTo(" xx  ");
    }

    @Test
    public void padBothEmpty() {
        assertThat(ExtraStrings.padBoth("", 1, ' ')).isEqualTo(" ");
        assertThat(ExtraStrings.padBoth(null, 1, ' ')).isEqualTo(" ");
    }

    @Test
    public void truncateEnd() {
        assertThat(ExtraStrings.truncateEnd("xyz", 0)).isEqualTo("");
        assertThat(ExtraStrings.truncateEnd("xyz", 1)).isEqualTo("x");
        assertThat(ExtraStrings.truncateEnd("xyz", 2)).isEqualTo("xy");
        assertThat(ExtraStrings.truncateEnd("xyz", 3)).isEqualTo("xyz");
        assertThat(ExtraStrings.truncateEnd("xyz", 4)).isEqualTo("xyz");
    }

    @Test(expected = NullPointerException.class)
    public void truncateEndNull() {
        ExtraStrings.truncateEnd(null, 0);
    }

    @Test
    public void truncateStart() {
        assertThat(ExtraStrings.truncateStart("xyz", 0)).isEqualTo("");
        assertThat(ExtraStrings.truncateStart("xyz", 1)).isEqualTo("z");
        assertThat(ExtraStrings.truncateStart("xyz", 2)).isEqualTo("yz");
        assertThat(ExtraStrings.truncateStart("xyz", 3)).isEqualTo("xyz");
        assertThat(ExtraStrings.truncateStart("xyz", 4)).isEqualTo("xyz");
    }

    @Test(expected = NullPointerException.class)
    public void truncateStartNull() {
        ExtraStrings.truncateStart(null, 0);
    }

    @Test
    public void truncateMiddleOdd() {
        assertThat(ExtraStrings.truncateMiddle("abcdefg", 2)).isEqualTo("..");
        assertThat(ExtraStrings.truncateMiddle("abcdefg", 3)).isEqualTo("..g");
        assertThat(ExtraStrings.truncateMiddle("abcdefg", 4)).isEqualTo("a..g");
        assertThat(ExtraStrings.truncateMiddle("abcdefg", 5)).isEqualTo("a..fg");
        assertThat(ExtraStrings.truncateMiddle("abcdefg", 6)).isEqualTo("ab..fg");
        assertThat(ExtraStrings.truncateMiddle("abcdefg", 7)).isEqualTo("abcdefg");
    }

    @Test
    public void truncateMiddleEven() {
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 2)).isEqualTo("..");
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 3)).isEqualTo("..h");
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 4)).isEqualTo("a..h");
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 5)).isEqualTo("a..gh");
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 6)).isEqualTo("ab..gh");
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 7)).isEqualTo("ab..fgh");
        assertThat(ExtraStrings.truncateMiddle("abcdefgh", 8)).isEqualTo("abcdefgh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void truncateMiddle0() {
        ExtraStrings.truncateMiddle("xyz", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void truncateMiddle1() {
        ExtraStrings.truncateMiddle("xyz", 1);
    }

    @Test(expected = NullPointerException.class)
    public void truncateMiddleNull() {
        ExtraStrings.truncateMiddle(null, 2);
    }

    @Test
    public void removePrefixNull() {
        assertThat(ExtraStrings.removePrefix(null, "abc")).isNull();
        assertThat(ExtraStrings.removePrefix("abc", null)).isEqualTo("abc");
    }

    @Test
    public void removePrefixMatching() {
        assertThat(ExtraStrings.removePrefix("wxyz", "wx")).isEqualTo("yz");
    }

    @Test
    public void removePrefixNotMatching() {
        assertThat(ExtraStrings.removePrefix("wxyz", "yz")).isEqualTo("wxyz");
    }

    @Test
    public void removeSuffixNull() {
        assertThat(ExtraStrings.removeSuffix(null, "abc")).isNull();
        assertThat(ExtraStrings.removeSuffix("abc", null)).isEqualTo("abc");
    }

    @Test
    public void removeSuffixMatching() {
        assertThat(ExtraStrings.removeSuffix("wxyz", "yz")).isEqualTo("wx");
    }

    @Test
    public void removeSuffixNotMatching() {
        assertThat(ExtraStrings.removeSuffix("wxyz", "wx")).isEqualTo("wxyz");
    }

    @Test
    public void beforeFirstMatching() {
        assertThat(ExtraStrings.beforeFirst("foobar", 'o')).isEqualTo("f");
        assertThat(ExtraStrings.beforeFirst("wxyz", 'y')).isEqualTo("wx");
    }

    @Test
    public void beforeFirstNotMatching() {
        assertThat(ExtraStrings.beforeFirst("foobar", 'x')).isEqualTo("foobar");
        assertThat(ExtraStrings.beforeFirst("wxyz", 'a')).isEqualTo("wxyz");
    }

    @Test
    public void beforeFirstNull() {
        assertThat(ExtraStrings.beforeFirst(null, 'a')).isNull();
    }

    @Test
    public void beforeLastMatching() {
        assertThat(ExtraStrings.beforeLast("foobar", 'o')).isEqualTo("fo");
        assertThat(ExtraStrings.beforeLast("wxyz", 'y')).isEqualTo("wx");
    }

    @Test
    public void beforeLastNotMatching() {
        assertThat(ExtraStrings.beforeLast("foobar", 'x')).isEqualTo("foobar");
        assertThat(ExtraStrings.beforeLast("wxyz", 'a')).isEqualTo("wxyz");
    }

    @Test
    public void beforeLastNull() {
        assertThat(ExtraStrings.beforeLast(null, 'a')).isNull();
    }

    @Test
    public void afterFirstMatching() {
        assertThat(ExtraStrings.afterFirst("foobar", 'o')).isEqualTo("obar");
        assertThat(ExtraStrings.afterFirst("wxyz", 'y')).isEqualTo("z");
    }

    @Test
    public void afterFirstNotMatching() {
        assertThat(ExtraStrings.afterFirst("foobar", 'x')).isEqualTo("");
        assertThat(ExtraStrings.afterFirst("wxyz", 'a')).isEqualTo("");
    }

    @Test
    public void afterFirstNull() {
        assertThat(ExtraStrings.afterFirst(null, 'a')).isNull();
    }

    @Test
    public void afterLastMatching() {
        assertThat(ExtraStrings.afterLast("foobar", 'o')).isEqualTo("bar");
        assertThat(ExtraStrings.afterLast("wxyz", 'y')).isEqualTo("z");
    }

    @Test
    public void afterLastNotMatching() {
        assertThat(ExtraStrings.afterLast("foobar", 'x')).isEqualTo("");
        assertThat(ExtraStrings.afterLast("wxyz", 'a')).isEqualTo("");
    }

    @Test
    public void afterLastNull() {
        assertThat(ExtraStrings.afterLast(null, 'a')).isNull();
    }

    @Test
    public void splitOnFirstNull() {
        assertThat(ExtraStrings.splitOnFirst(null, 'a', ExtraStringsTest::joinWithHash)).isNull();
    }

    @Test
    public void splitOnFirstMatching() {
        assertThat(ExtraStrings.splitOnFirst("foobar", 'f', ExtraStringsTest::joinWithHash)).isEqualTo("#oobar");
        assertThat(ExtraStrings.splitOnFirst("foobar", 'o', ExtraStringsTest::joinWithHash)).isEqualTo("f#obar");
        assertThat(ExtraStrings.splitOnFirst("foobar", 'r', ExtraStringsTest::joinWithHash)).isEqualTo("fooba#");
    }

    @Test
    public void splitOnFirstNotMatching() {
        assertThat(ExtraStrings.splitOnFirst("foobar", 'x', ExtraStringsTest::joinWithHash)).isEqualTo("foobar#");
    }

    @Test
    public void splitOnLastNull() {
        assertThat(ExtraStrings.splitOnLast(null, 'a', ExtraStringsTest::joinWithHash)).isNull();
    }

    @Test
    public void splitOnLastMatching() {
        assertThat(ExtraStrings.splitOnLast("foobar", 'f', ExtraStringsTest::joinWithHash)).isEqualTo("#oobar");
        assertThat(ExtraStrings.splitOnLast("foobar", 'o', ExtraStringsTest::joinWithHash)).isEqualTo("fo#bar");
        assertThat(ExtraStrings.splitOnLast("foobar", 'r', ExtraStringsTest::joinWithHash)).isEqualTo("fooba#");
    }

    @Test
    public void splitOnLastNotMatching() {
        assertThat(ExtraStrings.splitOnLast("foobar", 'x', ExtraStringsTest::joinWithHash)).isEqualTo("#foobar");
    }

    @Test
    public void splitNoDelim() {
        assertThat(ExtraStrings.split("foobar", ',')).containsExactly("foobar").inOrder();
    }

    @Test
    public void splitDelimPrefix() {
        assertThat(ExtraStrings.split(",foobar", ',')).containsExactly("", "foobar").inOrder();
    }

    @Test
    public void splitDelimSuffix() {
        assertThat(ExtraStrings.split("foobar,", ',')).containsExactly("foobar", "").inOrder();
    }

    @Test
    public void split() {
        assertThat(ExtraStrings.split("foo,bar,gus", ',')).containsExactly("foo", "bar", "gus").inOrder();
    }

    private static String joinWithHash(CharSequence a, CharSequence b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return new StringJoiner("#").add(a).add(b).toString();
    }

}
