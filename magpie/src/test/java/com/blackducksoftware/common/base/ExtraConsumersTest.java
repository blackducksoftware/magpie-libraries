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

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.junit.Test;

/**
 * Tests for {@link ExtraConsumers}
 *
 * @author jgustie
 */
public class ExtraConsumersTest {

    @Test
    public void withBoth_argumentOrder() {
        List<String> strings = new ArrayList<>();
        BiConsumer<String, String> bc = ExtraConsumers.withBoth(strings::add);
        bc.accept("a", "b");
        assertThat(strings).containsExactly("a", "b").inOrder();
    }

    @Test
    public void onlyIf_notMatched() {
        AtomicBoolean called = new AtomicBoolean();
        ExtraConsumers.onlyIf((x, y) -> false, (x, y) -> called.set(true)).accept(null, null);
        assertThat(called.get()).isFalse();
    }

    @Test
    public void onlyIf_matched() {
        AtomicBoolean called = new AtomicBoolean();
        ExtraConsumers.onlyIf((x, y) -> true, (x, y) -> called.set(true)).accept(null, null);
        assertThat(called.get()).isTrue();
    }

}
