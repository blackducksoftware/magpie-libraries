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

import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.Test;

/**
 * Tests for {@code ExtraFunctions}.
 *
 * @author jgustie
 */
public class ExtraFunctionsTest {

    @Test
    public void nullSafeFunction() {
        assertThat(ExtraFunctions.nullSafe(Objects::requireNonNull).apply(null)).isNull();
    }

    @Test
    public void nullSafeCompose() {
        // NOTE: This verifies that the composed method is never actually invoked!
        assertThat(ExtraFunctions.nullSafe(Objects::requireNonNull).compose(Objects::requireNonNull).apply(null)).isNull();
    }

    @Test
    public void nullSafeAndThen() {
        // NOTE: This verifies that the follow up method is never actually invoked!
        assertThat(ExtraFunctions.nullSafe(Objects::requireNonNull).andThen(Objects::requireNonNull).apply(null)).isNull();
    }

    @Test
    public void curryFirst() {
        Map<String, Integer> holder = new HashMap<>();
        holder.put("test", 1);
        assertThat(ExtraFunctions.curry("test", holder::put).apply(2)).isEqualTo(1);
        assertThat(holder).containsEntry("test", 2);
    }

    @Test
    public void currySecond() {
        Map<String, Integer> holder = new HashMap<>();
        holder.put("test", 1);
        assertThat(ExtraFunctions.curry(holder::put, 2).apply("test")).isEqualTo(1);
        assertThat(holder).containsEntry("test", 2);
    }

}
