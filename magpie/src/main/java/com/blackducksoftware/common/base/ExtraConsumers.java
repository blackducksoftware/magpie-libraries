/*
 * Copyright 2018 Synopsys, Inc.
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

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * Extra consumer helpers.
 *
 * @author jgustie
 */
public class ExtraConsumers {

    /**
     * Returns a {@link BiConsumer} that invokes the supplied consumer with both arguments (in argument order).
     */
    public static <T> BiConsumer<T, T> withBoth(Consumer<? super T> c) {
        return (a, b) -> {
            c.accept(a);
            c.accept(b);
        };
    }

    /**
     * Returns a consumer that is only invoked when the supplied predicate accepts the input.
     */
    public static <T, U> BiConsumer<T, U> onlyIf(BiPredicate<T, U> p, BiConsumer<T, U> c) {
        return (t, u) -> {
            if (p.test(t, u)) c.accept(t, u);
        };
    }

}
