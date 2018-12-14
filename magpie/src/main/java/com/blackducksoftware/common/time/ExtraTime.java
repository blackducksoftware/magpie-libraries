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

import java.time.Duration;

/**
 * Extra time helpers.
 *
 * @author jgustie
 */
public class ExtraTime {

    /**
     * Constant for adjusting seconds to nanoseconds.
     */
    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000;

    /**
     * Constant for adjusting nanoseconds to seconds.
     */
    private static final double SECONDS_PER_NANOSECOND = 1E-9;

    /**
     * Obtains a {@code Duration} representing a fractional number of seconds.
     */
    public static Duration ofSeconds(double seconds) {
        return Duration.ofSeconds((long) seconds, (long) (seconds * NANOSECONDS_PER_SECOND) % NANOSECONDS_PER_SECOND);
    }

    /**
     * Returns a fractional number of seconds for a {@code Duration}.
     */
    public static double toSeconds(Duration duration) {
        return duration.getSeconds() + (duration.getNano() * SECONDS_PER_NANOSECOND);
    }

}
