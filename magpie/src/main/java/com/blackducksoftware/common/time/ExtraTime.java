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

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Extra time helpers.
 * 
 * @author jgustie
 */
public class ExtraTime {

    /**
     * Implementation that supports formatting a duration.
     */
    private static final class FormattableDuration implements Formattable {

        /**
         * Default precision to use when formatting.
         */
        private static final int DEFAULT_PRECISION = 4;

        /**
         * The duration being formatted.
         */
        private final Duration duration;

        private FormattableDuration(Duration duration) {
            this.duration = Objects.requireNonNull(duration);
        }

        @Override
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            long nanos = duration.toNanos();
            TimeUnit unit = chooseUnit(nanos);
            double value = (double) nanos / NANOSECONDS.convert(1, unit);
            String abbreviation = abbreviate(unit);

            StringBuilder format = new StringBuilder(8);
            format.append("%");
            if (width > 0) {
                format.append(Math.max(0, width - (1 + abbreviation.length())));
            }
            format.append('.');
            format.append(precision < 0 ? DEFAULT_PRECISION : precision);
            format.append("g %s");
            formatter.format(format.toString(), value, abbreviation);
        }

        @Override
        public String toString() {
            Formatter formatter = new Formatter(new StringBuilder(10));
            formatTo(formatter, 0, -1, -1);
            return formatter.toString();
        }

        private static TimeUnit chooseUnit(long nanos) {
            if (NANOSECONDS.toDays(nanos) > 0) {
                return DAYS;
            } else if (NANOSECONDS.toHours(nanos) > 0) {
                return HOURS;
            } else if (NANOSECONDS.toMinutes(nanos) > 0) {
                return MINUTES;
            } else if (NANOSECONDS.toSeconds(nanos) > 0) {
                return SECONDS;
            } else if (NANOSECONDS.toMillis(nanos) > 0) {
                return MILLISECONDS;
            } else if (NANOSECONDS.toMicros(nanos) > 0) {
                return MICROSECONDS;
            } else {
                return NANOSECONDS;
            }
        }

        private static String abbreviate(TimeUnit unit) {
            switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError("unknown unit: " + unit);
            }
        }
    }

    /**
     * Constant for adjusting seconds to nanoseconds.
     */
    private static final long NANOSECONDS_PER_SECOND = 1000 * 1000 * 1000;

    /**
     * Obtains a {@code Duration} representing a fractional number of seconds.
     */
    public static Duration ofSeconds(double seconds) {
        return Duration.ofSeconds((long) seconds, (long) (seconds * NANOSECONDS_PER_SECOND) % NANOSECONDS_PER_SECOND);
    }

    /**
     * Formatter for the supplied duration.
     */
    public static Formattable print(Duration duration) {
        return new FormattableDuration(duration);
    }

}
