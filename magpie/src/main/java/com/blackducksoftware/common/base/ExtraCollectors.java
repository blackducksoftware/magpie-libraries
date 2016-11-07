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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Extra stream collectors.
 *
 * @author jgustie
 */
public class ExtraCollectors {

    /**
     * Returns a collector that will return the only element in a stream. The resulting optional will be empty if the
     * stream is empty; if the stream has multiple elements then collection will fail with an
     * {@code IllegalStateException}.
     */
    public static <E> Collector<E, ?, Optional<E>> getOnly() {
        // https://plus.google.com/+LouisWasserman
        // http://stackoverflow.com/questions/22694884/filter-java-stream-to-1-and-only-1-element/22695424#22695424
        return Collector.of(
                AtomicReference<E>::new,
                (ref, e) -> {
                    if (!ref.compareAndSet(null, e)) {
                        throw new IllegalStateException("Multiple values");
                    }
                },
                (ref1, ref2) -> {
                    if (ref1.get() == null) {
                        return ref2;
                    } else if (ref2.get() != null) {
                        throw new IllegalStateException("Multiple values");
                    } else {
                        return ref1;
                    }
                },
                ref -> Optional.ofNullable(ref.get()),
                Collector.Characteristics.UNORDERED);
    }

    /**
     * Returns a collector which builds an {@code ImmutableList}.
     */
    public static <T> Collector<T, ImmutableList.Builder<T>, List<T>> toImmutableList() {
        // http://jakewharton.com
        // https://gist.github.com/JakeWharton/9734167
        return Collector.of(
                ImmutableList.Builder::new,
                ImmutableList.Builder::add,
                (l, r) -> l.addAll(r.build()),
                ImmutableList.Builder<T>::build);
    }

    /**
     * Returns a collector which builds an {@code ImmutableSet}.
     */
    public static <T> Collector<T, ImmutableSet.Builder<T>, Set<T>> toImmutableSet() {
        return Collector.of(
                ImmutableSet.Builder::new,
                ImmutableSet.Builder::add,
                (l, r) -> l.addAll(r.build()),
                ImmutableSet.Builder<T>::build,
                Collector.Characteristics.UNORDERED);
    }

    private ExtraCollectors() {
        assert false;
    }
}
