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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;

/**
 * Extra string utilities.
 *
 * @author jgustie
 */
public final class ExtraStrings {

    /**
     * Returns an optional string that is present only if the supplied value is not empty. For example,
     * {@code ofEmpty(null).isPresent() == ofEmpty("").isPresent()}.
     */
    public static Optional<String> ofEmpty(@Nullable CharSequence value) {
        return Optional.ofNullable(value).filter(cs -> cs.length() > 0).map(CharSequence::toString);
    }

    /**
     * Returns an optional string that is present only if the supplied value contains non-whitespace characters. For
     * example, {@code ofBlank(" foo ").map(String::trim).get().equals("foo")}
     */
    public static Optional<String> ofBlank(@Nullable CharSequence value) {
        return Optional.ofNullable(value).filter(CharMatcher.whitespace().negate()::matchesAnyOf).map(CharSequence::toString);
    }

    /**
     * Returns a string ensuring that it begins with the specified prefix. For example,
     * {@code ensurePrefix("/", "/foo").equals("/foo")} and {@code ensurePrefix("/", "foo").equals("/foo")}.
     */
    @Nullable
    public static String ensurePrefix(CharSequence prefix, @Nullable CharSequence value) {
        Objects.requireNonNull(prefix);
        if (value == null) {
            return null;
        } else if (startsWith(value, prefix, 0)) {
            return value.toString();
        } else {
            return new StringBuilder(prefix.length() + value.length()).append(prefix).append(value).toString();
        }
    }

    /**
     * Ensures that two strings are joined by with the specified delimiter. For example,
     * {@code ensureDelimiter("foo", "/", "bar").equals("foo/bar")} as does
     * {@code ensureDelimiter("foo/", "/", "bar").equals("foo/bar")} and
     * {@code ensureDelimiter("foo", "/", "/bar").equals("foo/bar")}.
     */
    public static String ensureDelimiter(@Nullable CharSequence start, CharSequence delimiter, @Nullable CharSequence end) {
        Objects.requireNonNull(delimiter);
        if (start == null && end == null) {
            return delimiter.toString();
        }

        boolean delimit = start == null || !startsWith(start, delimiter, start.length() - delimiter.length());
        int endOffset = 0;
        if (end != null && startsWith(end, delimiter, 0)) {
            if (delimit) {
                delimit = false;
            } else {
                endOffset = delimiter.length();
            }
        }

        int len = (start != null ? start.length() : 0) + (delimit ? delimiter.length() : 0) + (end != null ? end.length() - endOffset : 0);
        StringBuilder result = new StringBuilder(len);
        if (start != null) {
            result.append(start);
        }
        if (delimit) {
            result.append(delimiter);
        }
        if (end != null) {
            result.append(end, endOffset, end.length());
        }
        return result.toString();
    }

    /**
     * Returns a string ensuring that it does not start with the specified prefix. For example,
     * {@code removePrefix("foo.bar", "foo.").equals("bar")} and
     * {@code removePrefix("gus.bar", "foo.").equals("gus.bar")}.
     */
    public static String removePrefix(@Nullable CharSequence value, @Nullable CharSequence prefix) {
        if (value != null && prefix != null) {
            if (startsWith(value, prefix, 0)) {
                return value.subSequence(prefix.length(), value.length()).toString();
            } else {
                return value.toString();
            }
        } else {
            return value != null ? value.toString() : null;
        }
    }

    /**
     * Returns a string ensuring that it does not end with the specified suffix. For example,
     * {@code removeSuffix("foo.bar", ".bar").equals("foo")} and
     * {@code removeSuffix("foo.gus", ".bar").equals("foo.gus")}.
     */
    @Nullable
    public static String removeSuffix(@Nullable CharSequence value, @Nullable CharSequence suffix) {
        if (value != null && suffix != null) {
            int pos = value.length() - suffix.length();
            if (startsWith(value, suffix, pos)) {
                return value.subSequence(0, pos).toString();
            } else {
                return value.toString();
            }
        } else {
            return value != null ? value.toString() : null;
        }
    }

    /**
     * Returns a string of all the characters before the last occurrence of the specified character. For example,
     * {@code beforeLast("foobar", 'b').equals("foo")} and {@code beforeLast("foobar", 'x').equals("foobar")}.
     */
    @Nullable
    public static String beforeLast(@Nullable CharSequence value, char c) {
        if (value != null) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (value.charAt(i) == c) {
                    return value.subSequence(0, i).toString();
                }
            }
            return value.toString();
        } else {
            return null;
        }
    }

    /**
     * Returns a string of all the characters after the last occurrence of the specified character. For example,
     * {@code afterLast("foobar", 'b').equals("ar")} and {@code afterLast("foobar", 'x').equals("foobar")}.
     */
    public static String afterLast(@Nullable CharSequence value, char c) {
        if (value != null) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (value.charAt(i) == c) {
                    return value.subSequence(i + 1, value.length()).toString();
                }
            }
            return value.toString();
        } else {
            return null;
        }
    }

    /**
     * Internal implementation for testing arbitrary character sequence prefixes.
     * <p>
     * Note that you can use this to implement "ends with" as well:
     *
     * <pre>
     * startsWith(value, suffix, value.length() - suffix.length())
     * </pre>
     */
    private static boolean startsWith(CharSequence value, CharSequence prefix, int offset) {
        int o = prefix.length();
        int pi = 0;
        int vi = offset;
        if (offset < 0 || offset > value.length() - prefix.length()) {
            return false;
        }
        while (--o >= 0) {
            if (prefix.charAt(pi++) != value.charAt(vi++)) {
                return false;
            }
        }
        return true;
    }

    private ExtraStrings() {
        assert false;
    }
}
