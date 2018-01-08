/*
 * Copyright 2018 Black Duck Software, Inc.
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Extra collector helpers.
 *
 * @author jgustie
 */
public class ExtraCollectors {

    /**
     * Returns a {@code Collector} that accumulates elements into a {@code Map} whose keys and values are the result of
     * applying the provided mapping functions to the input elements.
     */
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper, Supplier<M> mapSupplier) {
        return Collectors.toMap(keyMapper, valueMapper, throwingMerger(), mapSupplier);
    }

    /**
     * Returns a {@code Collector} that indexes elements into a map using the supplied key mapper.
     */
    public static <T, K> Collector<T, ?, Map<K, T>> index(Function<? super T, ? extends K> keyMapper) {
        return Collectors.toMap(keyMapper, Function.identity());
    }

    /**
     * Returns a collector that accumulates entries into a map.
     */
    public static <K, V> Collector<Map.Entry<K, V>, ?, Map<K, V>> toMapOfEntries() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * Returns a {@code Collector} that accumulates the input enumeration into a new {@code Set} of name values.
     */
    public static <E extends Enum<E>> Collector<E, ?, Set<String>> enumNames() {
        return Collectors.mapping(E::name, Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    /**
     * Returns a {@code Collector} that accumulates the input enumeration into a new {@code List} of {@code toString}
     * values.
     */
    public static <E extends Enum<E>> Collector<E, ?, List<String>> enumStrings() {
        return Collectors.mapping(E::toString, Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * Returns a {@code Collector} that accumulates the input enumeration into a new {@code Set} of {@code toString}
     * values.
     */
    public static <E extends Enum<E>> Collector<E, ?, Set<String>> uniqueEnumStrings() {
        return Collectors.mapping(E::toString, Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    // TODO Should we also have the inverse (e.g. Collector<String, ?, EnumSet<E>> fromStringValues())?

    // TODO `Collector<E, ?, EnumSet<E>> toEnumSet(Class<E>)`
    // TODO `Collector<E, ?, EnumMap<E, U>> toEnumMap(Class<E>, Function<E, U>)`

    // This same functionality is part of the built-in collectors, it's just not public.
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    private ExtraCollectors() {
        assert false;
    }
}
