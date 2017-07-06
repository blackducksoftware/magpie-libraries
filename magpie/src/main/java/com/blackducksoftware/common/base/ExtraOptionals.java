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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.blackducksoftware.common.annotations.Obsolete;

/**
 * Extra optional helpers.
 *
 * @author jgustie
 */
public final class ExtraOptionals {

    /**
     * If a value is present, performs the given action with the value, otherwise performs the given empty-based action.
     */
    @Obsolete(value = "Added in Java 9", see = "java.util.Optional#ifPresentOrElse")
    public static <T> void ifPresentOrElse(Optional<T> self, Consumer<? super T> action, Runnable emptyAction) {
        T value = self.orElse(null);
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    /**
     * If a value is present, returns an {@code Optional} describing the value, otherwise returns an {@code Optional}
     * produced by the supplying function.
     */
    @Obsolete(value = "Added in Java 9", see = "java.util.Optional#or")
    public static <T> Optional<T> or(Optional<T> self, Supplier<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (self.isPresent()) {
            return self;
        } else {
            @SuppressWarnings("unchecked")
            Optional<T> r = (Optional<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing only that value, otherwise returns an empty
     * {@code Stream}.
     */
    @Obsolete(value = "Added in Java 9", see = "java.util.Optional#stream")
    public static <T> Stream<T> stream(Optional<T> self) {
        if (!self.isPresent()) {
            return Stream.empty();
        } else {
            T value = self.get();
            return Stream.of(value);
        }
    }

    /**
     * If a value is present in the {@code OptionalDouble}, apply the provided double mapping function to it, and if
     * the result is non-null, return an {@code Optional} describing the result. Otherwise return an empty
     * {@code Optional}.
     */
    public static <T> Optional<T> mapToObj(OptionalDouble self, DoubleFunction<? extends T> mapper) {
        Objects.requireNonNull(mapper);
        if (self.isPresent()) {
            return Optional.ofNullable(mapper.apply(self.getAsDouble()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * If a value is present in the {@code OptionalInt}, apply the provided integer mapping function to it, and if the
     * result is non-null, return an {@code Optional} describing the result. Otherwise return an empty {@code Optional}.
     */
    public static <T> Optional<T> mapToObj(OptionalInt self, IntFunction<? extends T> mapper) {
        Objects.requireNonNull(mapper);
        if (self.isPresent()) {
            return Optional.ofNullable(mapper.apply(self.getAsInt()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * If a value is present in the {@code OptionalLong}, apply the provided long mapping function to it, and if the
     * result is non-null, return an {@code Optional} describing the result. Otherwise return an empty {@code Optional}.
     */
    public static <T> Optional<T> mapToObj(OptionalLong self, LongFunction<? extends T> mapper) {
        Objects.requireNonNull(mapper);
        if (self.isPresent()) {
            return Optional.ofNullable(mapper.apply(self.getAsLong()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * A function for filtering an optional by type. For example:
     *
     * <pre>
     * String str = Optional.of(obj).flatMap(ofType(String.class)).orElse(null);
     * </pre>
     *
     * Which is another way of doing:
     *
     * <pre>
     * String str = Optional.of(obj).filter(String.class::isInstance).map(String.class::cast).orElse(null);
     * </pre>
     *
     * Which is a convoluted way of doing:
     *
     * <pre>
     * String str = obj instanceof String ? (String) obj : null;
     * </pre>
     */
    public static <T> Function<Object, Optional<T>> ofType(Class<T> type) {
        Objects.requireNonNull(type);
        return obj -> type.isInstance(obj) ? Optional.of(type.cast(obj)) : Optional.empty();
    }

    /**
     * If both values are present, combines them using the supplied function, if the result is non-null, return an
     * {@code Optional} describing the result. Otherwise return an empty {@code Optional}.
     */
    public static <A, B, C> Optional<C> and(Optional<A> a, Optional<B> b, BiFunction<? super A, ? super B, C> combiner) {
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(b);
        if (a.isPresent() && b.isPresent()) {
            return combiner.andThen(Optional::ofNullable).apply(a.get(), b.get());
        } else {
            return Optional.empty();
        }
    }

    /**
     * If both values are present, performs the given action using both values.
     */
    // TODO Name?
    // public static <A, B> void acceptIfBothPresent(Optional<A> a, Optional<B> b, BiConsumer<? super A, ? super B>
    // consumer) {
    // Objects.requireNonNull(consumer);
    // Objects.requireNonNull(b);
    // if (a.isPresent() && b.isPresent()) {
    // consumer.accept(a.get(), b.get());
    // }
    // }

    private ExtraOptionals() {
        assert false;
    }
}
