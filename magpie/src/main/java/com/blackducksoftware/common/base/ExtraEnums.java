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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.annotations.Beta;

/**
 * Extra enumeration helpers. Most of this functionality is generally "questionable", however as with most rules, there
 * are times when it is really useful to break them.
 *
 * @author jgustie
 */
public final class ExtraEnums {

    /**
     * Returns all of the string representations for an enumerated type.
     */
    public static <E extends Enum<E>> List<String> stringValues(Class<E> enumClass) {
        return Stream.of(enumClass.getEnumConstants()).map(E::toString)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    /**
     * Returns the string representations for the supplied enumerated values.
     */
    @SafeVarargs
    public static <E extends Enum<E>> List<String> stringValues(E... enumValues) {
        return Stream.of(enumValues).map(E::toString)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    /**
     * Returns the string representations for the supplied enumerated values.
     */
    public static <E extends Enum<E>> List<String> stringValues(Iterable<E> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false).map(E::toString)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    /**
     * Returns the unique string representations for an enumerated type.
     */
    public static <E extends Enum<E>> Set<String> uniqueStringValues(Class<E> enumClass) {
        return Stream.of(enumClass.getEnumConstants()).map(E::toString)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns the unique string representations for the supplied enumerated values.
     */
    @SafeVarargs
    public static <E extends Enum<E>> Set<String> uniqueStringValues(E... enumValues) {
        return Stream.of(enumValues).map(E::toString)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns the unique string representations for the supplied enumerated values.
     */
    public static <E extends Enum<E>> Set<String> uniqueStringValues(Iterable<E> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false).map(E::toString)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns all of names for an enumerated type.
     */
    public static <E extends Enum<E>> Set<String> names(Class<E> enumClass) {
        return Stream.of(enumClass.getEnumConstants()).map(E::name)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns the names for the supplied enumerated values.
     */
    @SafeVarargs
    public static <E extends Enum<E>> Set<String> names(E... enumValues) {
        return Stream.of(enumValues).map(E::name)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns the names for the supplied enumerated values.
     */
    public static <E extends Enum<E>> Set<String> names(Iterable<E> enumValues) {
        return StreamSupport.stream(enumValues.spliterator(), false).map(E::name)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
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
