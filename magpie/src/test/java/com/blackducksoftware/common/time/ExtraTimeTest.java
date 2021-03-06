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
package com.blackducksoftware.common.time;

import static com.google.common.truth.Truth.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Tests for {@code ExtraTime}.
 *
 * @author jgustie
 */
public class ExtraTimeTest {

    private static final double NANOSECONDS_PER_SECOND = 1E9;

    @Test
    public void ofSeconds() {
        long nanos = 654321;
        nanos += TimeUnit.SECONDS.toNanos(10);
        nanos += TimeUnit.MILLISECONDS.toNanos(987);
        assertThat(ExtraTime.ofSeconds(nanos / NANOSECONDS_PER_SECOND)).isEqualTo(Duration.ofNanos(nanos));
    }

}
