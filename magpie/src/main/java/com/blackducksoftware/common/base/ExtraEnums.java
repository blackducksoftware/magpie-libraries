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

import static com.blackducksoftware.common.base.ExtraStrings.removePrefix;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.MoreCollectors.toOptional;

import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Enums;

/**
 * Extra enumeration helpers. Most of this functionality is generally "questionable", however as with most rules, there
 * are times when it is really useful to break them.
 *
 * @author jgustie
 */
public final class ExtraEnums {

    /**
     * Enumerated constant search interface.
     */
    public static class EnumSearcher<E extends Enum<E>> {

        private final boolean start;

        private final Class<E> enumType;

        private final BiPredicate<String, String> filter;

        private EnumSearcher(Class<E> enumType) {
            this.start = true;
            this.enumType = Objects.requireNonNull(enumType);
            this.filter = Objects::equals;
        }

        private EnumSearcher(Class<E> enumType, BiPredicate<String, String> filter) {
            this.start = false;
            this.enumType = Objects.requireNonNull(enumType);
            this.filter = Objects.requireNonNull(filter);
        }

        private EnumSearcher(EnumSearcher<E> searcher, Function<String, String> mapper) {
            this(searcher.enumType, (a, b) -> searcher.filter.test(mapper.apply(a), mapper.apply(b)));
        }

        /**
         * Find an enumerated constant by name. If multiple constants match (e.g. the search was performed ignoring
         * case) an {@code IllegalArgumentException} is thrown.
         */
        public Optional<E> byName(String name) {
            if (start) {
                return Enums.getIfPresent(enumType, name).toJavaUtil();
            } else {
                return withName(name).collect(toOptional());
            }
        }

        /**
         * Find an enumerated constant using it's {@code toString()} value. An {@code IllegalArgumentException} is
         * thrown if multiple values are matched.
         */
        public Optional<E> byToString(String value) {
            return withToString(value).collect(toOptional());
        }

        /**
         * Find all enumerated constants with the specified name. This stream will contain at most one element unless a
         * modifier has been applied since names are unique.
         */
        public Stream<E> withName(String name) {
            Objects.requireNonNull(name);
            return Arrays.stream(enumType.getEnumConstants()).filter(e -> filter.test(e.name(), name));
        }

        /**
         * Find all enumerated constants with the specified {@code toString()} value.
         */
        public Stream<E> withToString(String value) {
            Objects.requireNonNull(value);
            return Arrays.stream(enumType.getEnumConstants()).filter(e -> filter.test(e.toString(), value));
        }

        /**
         * Returns a new searcher that ignores the case sensitivity of the current searcher.
         */
        public EnumSearcher<E> ignoringCase() {
            return new EnumSearcher<>(this, String::toLowerCase);
        }

        /**
         * Returns a new searcher that ignores the supplied characters.
         */
        public EnumSearcher<E> ignoring(CharSequence chars) {
            return new EnumSearcher<>(this, CharMatcher.anyOf(chars)::removeFrom);
        }

        /**
         * Returns a new searcher that ignores the matching characters.
         */
        public EnumSearcher<E> ignoring(Predicate<? super Character> chars) {
            return new EnumSearcher<>(this, CharMatcher.forPredicate(chars::test)::removeFrom);
        }

        /**
         * Returns a new searcher that accepts lower-camel case values instead of the more commonly used
         * upper-underscore cased values (e.g. {@code upperUnderscoreUsingLowerCamel().byName("fooBar")} would find the
         * constant {@code FOO_BAR}).
         */
        public EnumSearcher<E> usingLowerCamel() {
            return new EnumSearcher<>(enumType, (a, b) -> filter.test(UPPER_UNDERSCORE.to(LOWER_CAMEL, a), b));
        }

        /**
         * Returns a new searcher that ignores the specified prefix.
         */
        public EnumSearcher<E> startsWith(String prefix) {
            return new EnumSearcher<>(this, s -> removePrefix(s, prefix));
        }
    }

    /**
     * Search for an enumerated constant on the specified type.
     */
    public static <E extends Enum<E>> EnumSearcher<E> search(Class<E> enumType) {
        return new EnumSearcher<>(enumType);
    }

    /**
     * Attempts to find an enumerated value using {@link Enum#valueOf(Class, String)}.
     */
    // TODO Deprecate in favor of `search(enumType).byName(name)`
    public static <E extends Enum<E>> Optional<E> tryByName(Class<E> enumClass, String name) {
        return Enums.getIfPresent(enumClass, name).toJavaUtil();
    }

    /**
     * Attempts to find enumerated values by their {@code toString} representation.
     */
    // TODO Deprecate in favor of `search(enumClass).withToString(toStringValue)`
    public static <E extends Enum<E>> Stream<E> tryByToString(Class<E> enumClass, String toStringValue) {
        return search(enumClass).withToString(toStringValue);
    }

    /**
     * Returns the enum value declared before the supplied value or empty if the supplied value is first.
     */
    public static <E extends Enum<E>> Optional<E> previous(E enumValue) {
        if (enumValue.ordinal() > 0) {
            return Optional.of(enumValue.getDeclaringClass().getEnumConstants()[enumValue.ordinal() - 1]);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the enum value declared after the supplied value or empty if the supplied value is last.
     */
    public static <E extends Enum<E>> Optional<E> next(E enumValue) {
        E[] enumConstants = enumValue.getDeclaringClass().getEnumConstants();
        if (enumValue.ordinal() + 1 < enumConstants.length) {
            return Optional.of(enumConstants[enumValue.ordinal() + 1]);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds or removes the enumerated value from the supplied set based on a boolean flag. Returns {@code true} if the
     * set changed as a result of the operation.
     */
    public static <E extends Enum<E>> boolean set(Set<E> set, E value, boolean on) {
        // One would think this functionality would be part of EnumSet
        Objects.requireNonNull(value);
        return on ? set.add(value) : set.remove(value);
    }

    /**
     * Adds or removes the enumerated value from the supplied set.
     */
    public static <E extends Enum<E>> void flip(Set<E> set, E value) {
        // One would think this functionality would be part of EnumSet
        Objects.requireNonNull(value);
        if (set.contains(value)) {
            set.remove(value);
        } else {
            set.add(value);
        }
    }

    /**
     * Returns a set of enum constants whose ordinals correspond to the set bit positions in the supplied integer value.
     * This requires that the enum constants be defined in a fixed, well-known order; before trying to use this method,
     * be sure that precautions are taken to ensure that enum declarations are stable.
     * <p>
     * This method only works for the first 64 fields of the enumeration, to access fields beyond the limit of a
     * {@code long}, use {@link #fromBitSet(Class, BitSet)} instead.
     */
    @Beta
    public static <E extends Enum<E>> EnumSet<E> fromBitSet(Class<E> enumClass, long bitSet) {
        E[] enumConstants = enumClass.getEnumConstants();
        checkArgument((bitSet & Long.MAX_VALUE) >>> enumConstants.length == 0, "bit set overflows enum: %s", enumClass.getName());
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (E value : enumConstants) {
            if ((bitSet & (1L << value.ordinal())) != 0) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Returns a set of enum constants whose ordinals correspond to the set bit positions in the supplied bit set.
     * This requires that the enum constants be defined in a fixed, well-known order; before trying to use this method,
     * be sure that precautions are taken to ensure that enum declarations are stable.
     */
    @Beta
    public static <E extends Enum<E>> EnumSet<E> fromBitSet(Class<E> enumClass, BitSet bitSet) {
        E[] enumConstants = enumClass.getEnumConstants();
        checkArgument(bitSet.length() <= enumConstants.length, "bit set overflows enum: %s", enumClass.getName());
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (E value : enumConstants) {
            if (bitSet.get(value.ordinal())) {
                result.add(value);
            }
        }
        return result;
    }

    // TODO Int limits us to 32 enum values.
    @Beta
    public static <E extends Enum<E>> int toBitSet(Set<E> values) {
        int result = 0;
        for (E value : values) {
            result &= 1 << value.ordinal();
        }
        return result;
    }

    private ExtraEnums() {
        assert false;
    }
}
