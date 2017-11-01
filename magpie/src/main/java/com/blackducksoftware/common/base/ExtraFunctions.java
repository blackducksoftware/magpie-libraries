/*
 * Copyright 2017 Black Duck Software, Inc.
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
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Extra function helpers.
 *
 * @author jgustie
 */
public class ExtraFunctions {

    /**
     * Wraps the supplied function such that it is not invoked with {@code null} values.
     * <p>
     * NOTE: If the resulting function is composed or chained, those functions will not be invoked with null values
     * either, the {@code null} essentially always propagates through.
     */
    public static <T, R> Function<T, R> nullSafe(Function<T, R> f) {
        Objects.requireNonNull(f);
        return new Function<T, R>() {
            @Override
            public R apply(T t) {
                return Optional.ofNullable(t).map(f).orElse(null);
            }

            @Override
            public <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
                Objects.requireNonNull(before);
                // TODO Is this the right behavior or should we still invoke before even with a null value?
                return (V v) -> Optional.ofNullable(v).map(before).map(f).orElse(null);
            }

            @Override
            public <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
                Objects.requireNonNull(after);
                // TODO Is this the right behavior or should we still invoke after even with a null value?
                return (T t) -> Optional.ofNullable(t).map(f).map(after).orElse(null);
            }
        };
    }

    /**
     * Returns a function which evaluates the bi-function with the supplied first argument.
     */
    public static <T, U, R> Function<U, R> curry(@Nullable T t, BiFunction<T, U, R> f) {
        Objects.requireNonNull(f);
        return u -> f.apply(t, u);
    }

    /**
     * Returns a function which evaluates the bi-function with the supplied second argument.
     */
    public static <T, U, R> Function<T, R> curry(BiFunction<T, U, R> f, @Nullable U u) {
        Objects.requireNonNull(f);
        return t -> f.apply(t, u);
    }

    private ExtraFunctions() {
        assert false;
    }

}
