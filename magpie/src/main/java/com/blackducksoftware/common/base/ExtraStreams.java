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
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Iterators;
import com.google.common.collect.Streams;

/**
 * Extra stream helpers.
 *
 * @author jgustie
 */
public final class ExtraStreams {

    /**
     * A function for filtering a stream by type. For example:
     *
     * <pre>
     * List&lt;String&gt; strs = objs.stream().flatMap(ofType(String.class)).collect(toList());
     * </pre>
     *
     * Is another way of doing:
     *
     * <pre>
     * List&lt;String&gt; strs = objs.stream().filter(String.class::isInstance).map(String.class::cast).collect(toList());
     * </pre>
     *
     * Which is another way of doing:
     *
     * <pre>
     * List&lt;String&gt; strs = new ArrayList&lt;&gt;();
     * for (Object obj : objs) {
     *     if (obj instanceof String) {
     *         strs.add((String) obj);
     *     }
     * }
     * </pre>
     */
    public static <T> Function<Object, Stream<T>> ofType(Class<T> type) {
        Objects.requireNonNull(type);
        return obj -> type.isInstance(obj) ? Stream.of(type.cast(obj)) : Stream.empty();
    }

    /**
     * Returns a sequential {@link Stream} of the remaining contents of {@code enumeration}. Do not use
     * {@code enumeration} directly after passing it to this method.
     */
    public static <T> Stream<T> stream(Enumeration<T> enumeration) {
        return Streams.stream(Iterators.forEnumeration(enumeration));
    }

    /**
     * Returns a sequential {@link Stream} of enumeration constants for the specified class.
     */
    public static <E extends Enum<E>> Stream<E> stream(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants());
    }

    /**
     * Returns a stream from a potentially {@code null} source.
     */
    public static <T> Stream<T> streamNullable(@Nullable Iterable<T> iterable) {
        return iterable != null ? Streams.stream(iterable) : Stream.empty();
    }

    private ExtraStreams() {
        assert false;
    }
}
