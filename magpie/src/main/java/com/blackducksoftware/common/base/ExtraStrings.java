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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;

/**
 * Extra string utilities.
 *
 * @author jgustie
 */
public final class ExtraStrings {

    /**
     * Returns {@code true} if the supplied character sequence has a length greater then zero.
     */
    public static boolean isNotEmpty(CharSequence value) {
        return value.length() > 0;
    }

    /**
     * Returns an optional string that is present only if the supplied value is not empty. For example,
     * {@code ofEmpty(null).isPresent() == ofEmpty("").isPresent()}.
     */
    public static Optional<String> ofEmpty(@Nullable CharSequence value) {
        return Optional.ofNullable(value).filter(ExtraStrings::isNotEmpty).map(CharSequence::toString);
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
     * Returns a string ensuring that it ends with the specified suffix. For example,
     * {@code ensureSuffix("foo/", "/").equals("foo/")} and {@code ensureSuffix("foo", "/").equals("foo/")}.
     */
    @Nullable
    public static String ensureSuffix(@Nullable CharSequence value, CharSequence suffix) {
        Objects.requireNonNull(suffix);
        if (value == null) {
            return null;
        } else if (startsWith(value, suffix, value.length() - suffix.length())) {
            return value.toString();
        } else {
            return new StringBuilder(value.length() + suffix.length()).append(value).append(suffix).toString();
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
     * Joins a sequence of arbitrary parts with the supplied delimiter.
     */
    public static String ensureDelimiter(Iterable<?> parts, CharSequence delimiter) {
        StringJoiner joiner = new StringJoiner(delimiter);
        parts.forEach(o -> {
            if (o instanceof CharSequence) {
                joiner.add((CharSequence) o);
            } else {
                joiner.add(o.toString());
            }
        });
        return joiner.toString();
    }

    /**
     * Applies padding to both sides of a value.
     */
    public static String padBoth(@Nullable CharSequence value, int minLength, char padChar) {
        if (value == null) {
            char[] result = new char[minLength];
            Arrays.fill(result, padChar);
            return new String(result);
        } else if (value.length() >= minLength) {
            return value.toString();
        }
        StringBuilder result = new StringBuilder(minLength);
        for (int i = (int) Math.floor((minLength - value.length()) / 2.0); i > 0; --i) {
            result.append(padChar);
        }
        result.append(value);
        for (int i = result.length(); i < minLength; ++i) {
            result.append(padChar);
        }
        return result.toString();
    }

    /**
     * Truncates a value to a maximum size.
     */
    public static String truncateEnd(CharSequence value, int maxLength) {
        return value.length() > maxLength ? value.subSequence(0, maxLength).toString() : value.toString();
    }

    /**
     * Truncates a value to a maximum size.
     */
    public static String truncateStart(CharSequence value, int maxLength) {
        return value.length() > maxLength ? value.subSequence(value.length() - maxLength, value.length()).toString() : value.toString();
    }

    /**
     * Truncates a value to a maximum size. Unlike other truncate methods, the delimiter ".." is used to indicate
     * truncation occurred in the middle of the value. The specified {@code maxLength} must be at least 2 to accommodate
     * the delimiter (i.e. if the maximum length is 2 the resulting value will always be "..").
     */
    public static String truncateMiddle(CharSequence value, int maxLength) {
        if (maxLength < 2) {
            throw new IllegalArgumentException("maxLength must be a least 2 (was " + maxLength + ")");
        } else if (value.length() <= maxLength) {
            return value.toString();
        }

        StringBuilder result = new StringBuilder(maxLength);
        double split = (maxLength - 2) / 2.0;
        result.append(value.subSequence(0, (int) Math.floor(split)));
        result.append('.').append('.');
        result.append(value.subSequence(value.length() - (int) Math.ceil(split), value.length()));
        return result.toString();
    }

    /**
     * Returns a string ensuring that it does not start with the specified prefix. For example,
     * {@code removePrefix("foo.bar", "foo.").equals("bar")} and
     * {@code removePrefix("gus.bar", "foo.").equals("gus.bar")}.
     */
    @Nullable
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
     * Returns a string of all the characters before the first occurrence of the specified character. For example,
     * {@code beforeFirst("foobar", 'o').equals("f")} and {@code beforeFirst("foobar", 'x').equals("foobar")}.
     */
    @Nullable
    public static String beforeFirst(@Nullable CharSequence value, char c) {
        if (value != null) {
            for (int i = 0; i < value.length(); ++i) {
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
     * Returns a string of all the characters before the last occurrence of the specified character. For example,
     * {@code beforeLast("foobar", 'o').equals("fo")} and {@code beforeLast("foobar", 'x').equals("foobar")}.
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
     * Returns a string of all the characters after the first occurrence of the specified character. For example,
     * {@code afterFirst("foobar", 'o').equals("obar")} and {@code afterFirst("foobar", 'x').equals("")}.
     */
    @Nullable
    public static String afterFirst(@Nullable CharSequence value, char c) {
        if (value != null) {
            for (int i = 0; i < value.length(); ++i) {
                if (value.charAt(i) == c) {
                    return value.subSequence(i + 1, value.length()).toString();
                }
            }
            return "";
        } else {
            return null;
        }
    }

    /**
     * Returns a string of all the characters after the last occurrence of the specified character. For example,
     * {@code afterLast("foobar", 'o').equals("bar")} and {@code afterLast("foobar", 'x').equals("")}.
     */
    @Nullable
    public static String afterLast(@Nullable CharSequence value, char c) {
        if (value != null) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (value.charAt(i) == c) {
                    return value.subSequence(i + 1, value.length()).toString();
                }
            }
            return "";
        } else {
            return null;
        }
    }

    /**
     * Split the input on the first occurrence of the specified character and return the result of applying a function
     * to the parts.
     */
    @Nullable
    public static <R> R splitOnFirst(@Nullable CharSequence value, char c, BiFunction<String, String, R> joiner) {
        Objects.requireNonNull(joiner);
        if (value != null) {
            for (int i = 0; i < value.length(); ++i) {
                if (value.charAt(i) == c) {
                    return joiner.apply(value.subSequence(0, i).toString(), value.subSequence(i + 1, value.length()).toString());
                }
            }
            return joiner.apply(value.toString(), "");
        } else {
            return null;
        }
    }

    /**
     * Split the input on the last occurrence of the specified character and return the result of applying a function to
     * the parts.
     */
    @Nullable
    public static <R> R splitOnLast(@Nullable CharSequence value, char c, BiFunction<String, String, R> joiner) {
        Objects.requireNonNull(joiner);
        if (value != null) {
            for (int i = value.length() - 1; i >= 0; --i) {
                if (value.charAt(i) == c) {
                    return joiner.apply(value.subSequence(0, i).toString(), value.subSequence(i + 1, value.length()).toString());
                }
            }
            // TODO Should this use the same order as splitOnFirst?
            return joiner.apply("", value.toString());
        } else {
            return null;
        }
    }

    /**
     * Split the input on every occurrence of the specified character. A {@literal null} input produces an empty result.
     */
    public static Stream<String> split(@Nullable CharSequence value, char delim) {
        if (value == null) {
            return Stream.empty();
        }

        class CharSplitterSpliterator extends AbstractSpliterator<String> {
            private int pos;

            public CharSplitterSpliterator() {
                super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
            }

            @Override
            public boolean tryAdvance(Consumer<? super String> action) {
                int len = value.length();
                if (pos > len) {
                    return false;
                }
                for (int i = pos; i < len; ++i) {
                    if (value.charAt(i) == delim) {
                        action.accept(value.subSequence(pos, i).toString());
                        pos = i + 1;
                        return true;
                    }
                }
                action.accept(value.subSequence(pos, len).toString());
                pos = len + 1;
                return true;
            }
        }
        return StreamSupport.stream(new CharSplitterSpliterator(), false);
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
    static boolean startsWith(CharSequence value, CharSequence prefix, int offset) {
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
