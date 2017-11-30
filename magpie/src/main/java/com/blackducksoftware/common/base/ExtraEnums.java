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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.annotations.Beta;
import com.google.common.base.Enums;

/**
 * Extra enumeration helpers. Most of this functionality is generally "questionable", however as with most rules, there
 * are times when it is really useful to break them.
 *
 * @author jgustie
 */
public final class ExtraEnums {

    /**
     * Used to produce collections of enumeration derived values.
     */
    private static final class EnumCollectors {

        // TODO Should we just expose these publicly and call it a day?

        // TODO Should we also have the inverse (e.g. Collector<String, ?, EnumSet<E>> fromStringValues())?

        // TODO What about Collector<E, ?, EnumSet<E>> toEnumSet()? Or does that belong on ExtraCollectors?

        private static <E extends Enum<E>> Collector<E, ?, List<String>> stringValues() {
            return mapping(E::toString, collectingAndThen(toList(), Collections::unmodifiableList));
        }

        private static <E extends Enum<E>> Collector<E, ?, Set<String>> uniqueStringValues() {
            return mapping(E::toString, collectingAndThen(toSet(), Collections::unmodifiableSet));
        }

        private static <E extends Enum<E>> Collector<E, ?, Set<String>> names() {
            return mapping(E::name, collectingAndThen(toSet(), Collections::unmodifiableSet));
        }

    }

    /**
     * Returns a stream of enumeration constants.
     */
    public static <E extends Enum<E>> Stream<E> stream(Class<E> enumClass) {
        return Stream.of(enumClass.getEnumConstants());
    }

    /**
     * Returns all of the string representations for an enumerated type.
     */
    public static <E extends Enum<E>> List<String> stringValues(Class<E> enumClass) {
        return stream(enumClass).collect(EnumCollectors.stringValues());
    }

    /**
     * Returns the string representations for the supplied enumerated values.
     */
    @SafeVarargs
    public static <E extends Enum<E>> List<String> stringValues(E enumValue, E... enumValues) {
        return Stream.concat(Stream.of(enumValue), Stream.of(enumValues)).collect(EnumCollectors.stringValues());
    }

    /**
     * Returns the string representations for the supplied enumerated values.
     */
    public static <E extends Enum<E>> List<String> stringValues(Iterable<E> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false).collect(EnumCollectors.stringValues());
    }

    /**
     * Returns the unique string representations for an enumerated type.
     */
    public static <E extends Enum<E>> Set<String> uniqueStringValues(Class<E> enumClass) {
        return stream(enumClass).collect(EnumCollectors.uniqueStringValues());
    }

    /**
     * Returns the unique string representations for the supplied enumerated values.
     */
    @SafeVarargs
    public static <E extends Enum<E>> Set<String> uniqueStringValues(E enumValue, E... enumValues) {
        return Stream.concat(Stream.of(enumValue), Stream.of(enumValues)).collect(EnumCollectors.uniqueStringValues());
    }

    /**
     * Returns the unique string representations for the supplied enumerated values.
     */
    public static <E extends Enum<E>> Set<String> uniqueStringValues(Iterable<E> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false).collect(EnumCollectors.uniqueStringValues());
    }

    /**
     * Returns all of names for an enumerated type.
     */
    public static <E extends Enum<E>> Set<String> names(Class<E> enumClass) {
        return stream(enumClass).collect(EnumCollectors.names());
    }

    /**
     * Returns the names for the supplied enumerated values.
     */
    @SafeVarargs
    public static <E extends Enum<E>> Set<String> names(E enumValue, E... enumValues) {
        return Stream.concat(Stream.of(enumValue), Stream.of(enumValues)).collect(EnumCollectors.names());
    }

    /**
     * Returns the names for the supplied enumerated values.
     */
    public static <E extends Enum<E>> Set<String> names(Iterable<E> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false).collect(EnumCollectors.names());
    }

    /**
     * Attempts to find an enumerated value using {@link Enum#valueOf(Class, String)}.
     */
    public static <E extends Enum<E>> Optional<E> tryByName(Class<E> enumClass, String name) {
        return Enums.getIfPresent(enumClass, name).toJavaUtil();
    }

    /**
     * Attempts to find enumerated values by their {@code toString} representation.
     */
    public static <E extends Enum<E>> Stream<E> tryByToString(Class<E> enumClass, String toStringValue) {
        return stream(enumClass).filter(e -> Objects.equals(e.toString(), toStringValue));
    }

    /**
     * Returns a set of enum constants whose ordinals correspond to the set bit positions in the supplied integer value.
     * This requires that the enum constants be defined in a fixed, well-known order; before trying to use this method,
     * be sure that precautions are taken to ensure that enum declarations are stable.
     * <p>
     * This method only works for enums with no more then 64 constants, for larger enums use
     * {@link #fromBitSet(Class, BitSet)}.
     */
    @Beta
    public static <E extends Enum<E>> EnumSet<E> fromBitSet(Class<E> enumClass, long bitSet) {
        E[] enumConstants = enumClass.getEnumConstants();
        checkArgument(enumConstants.length <= 64, "jumbo enum, use BitSet instead long: %s", enumClass.getName());
        // TODO Check position of leftmost bit compared to the length?

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
        EnumSet<E> result = EnumSet.noneOf(enumClass);
        for (E value : enumClass.getEnumConstants()) {
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
