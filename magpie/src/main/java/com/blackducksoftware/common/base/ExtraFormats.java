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

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.blackducksoftware.common.io.BinaryByteUnit;
import com.blackducksoftware.common.io.ByteUnit;
import com.blackducksoftware.common.io.DecimalByteUnit;

/**
 * Utilities for formatting stuff.
 *
 * @author jgustie
 */
public class ExtraFormats {

    /**
     * Implementation that supports formatting a byte count.
     */
    public static class FormattableByteCount implements Formattable {
        private final long byteCount;

        private FormattableByteCount(long byteCount) {
            this.byteCount = byteCount;
        }

        /**
         * The byte count being formatted.
         */
        public long getByteCount() {
            return byteCount;
        }

        @Override
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            ByteUnit unit = chooseByteUnit(byteCount);
            double value = (double) byteCount / unit.toBytes(1);
            String format = computeFormat(flags, width, precision, abbreviate(unit));

            formatter.format(format, value);
        }

        @Override
        public String toString() {
            Formatter formatter = new Formatter(new StringBuilder(10));
            formatTo(formatter, 0, -1, -1);
            return formatter.toString();
        }

        // Default implementations only support a hardcoded raw byte count

        protected ByteUnit chooseByteUnit(long byteCount) {
            return c -> c;
        }

        protected String abbreviate(ByteUnit unit) {
            return "B";
        }
    }

    /**
     * Byte count implementation for a binary measurement system.
     */
    private static final class FormattableBinaryByteCount extends FormattableByteCount {
        private FormattableBinaryByteCount(long byteCount) {
            super(byteCount);
        }

        @Override
        protected ByteUnit chooseByteUnit(long byteCount) {
            return chooseBinaryByteUnit(byteCount);
        }

        @Override
        protected String abbreviate(ByteUnit unit) {
            return BINARY_BYTE_UNIT[((BinaryByteUnit) unit).ordinal()];
        }
    }

    /**
     * Byte count implementation for a decimal measurement system.
     */
    private static final class FormattableDecimalByteCount extends FormattableByteCount {
        private FormattableDecimalByteCount(long byteCount) {
            super(byteCount);
        }

        @Override
        protected ByteUnit chooseByteUnit(long byteCount) {
            return chooseDecimalByteUnit(byteCount);
        }

        @Override
        protected String abbreviate(ByteUnit unit) {
            return DECIMAL_BYTE_UNIT[((DecimalByteUnit) unit).ordinal()];
        }
    }

    /**
     * Implementation that supports formatting a duration.
     */
    public static final class FormattableDuration implements Formattable {
        private final Duration duration;

        private FormattableDuration(Duration duration) {
            this.duration = Objects.requireNonNull(duration);
        }

        /**
         * The duration being formatted.
         */
        public Duration getDuration() {
            return duration;
        }

        @Override
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            long nanos = duration.toNanos();
            TimeUnit unit = chooseTimeUnit(nanos);
            double value = (double) nanos / NANOSECONDS.convert(1, unit);
            String format = computeFormat(flags, width, precision, TIME_UNIT[unit.ordinal()]);

            formatter.format(format, value);
        }

        @Override
        public String toString() {
            Formatter formatter = new Formatter(new StringBuilder(10));
            formatTo(formatter, 0, -1, -1);
            return formatter.toString();
        }
    }

    /**
     * Abbreviations for {@code TimeUnit}.
     */
    private static final String[] TIME_UNIT = { "ns", "\u03bcs", "ms", "s", "min", "h", "d" };

    /**
     * Abbreviations for {@code BinaryByteUnit}.
     */
    private static final String[] BINARY_BYTE_UNIT = { "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB" };

    /**
     * Abbreviations for {@code DecimalByteUnit}.
     */
    private static final String[] DECIMAL_BYTE_UNIT = { "B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };

    /**
     * Formatter for the supplied duration.
     */
    public static FormattableDuration print(Duration duration) {
        return new FormattableDuration(duration);
    }

    /**
     * Formatter for the supplied amount of data.
     */
    public static FormattableByteCount print(long count, ByteUnit unit) {
        // The unit here serves three purpose:
        // 1. Distinguish the method signature
        // 2. Determine which measurement system to use (binary or decimal)
        // 3. Convert the supplied count into a count of bytes
        if (unit instanceof BinaryByteUnit) {
            return new FormattableBinaryByteCount(unit.toBytes(count));
        } else if (unit instanceof DecimalByteUnit) {
            return new FormattableDecimalByteCount(unit.toBytes(count));
        } else {
            return new FormattableByteCount(unit.toBytes(count));
        }
    }

    /**
     * Computes a format pattern consisting of a number an a placeholder for the
     */
    private static String computeFormat(int flags, int width, int precision, String abbreviation) {
        if (width < 0 && precision < 0) {
            return new StringBuilder(5 + abbreviation.length()).append("%.4g ").append(abbreviation).toString();
        } else {
            StringBuilder formatBuilder = new StringBuilder(8).append("%");
            if (width > 0) {
                formatBuilder.append(Math.max(0, width - (1 + abbreviation.length())));
            }
            return formatBuilder
                    .append('.').append(precision < 0 ? 4 : precision)
                    .append("g ")
                    .append(abbreviation)
                    .toString();
        }
    }

    /**
     * Selects a binary byte unit based on the number of bytes.
     */
    private static BinaryByteUnit chooseBinaryByteUnit(long byteCount) {
        if (BinaryByteUnit.BYTES.toExbibytes(byteCount) > 0) {
            return BinaryByteUnit.EXBIBYTES;
        } else if (BinaryByteUnit.BYTES.toPebibytes(byteCount) > 0) {
            return BinaryByteUnit.PEBIBYTES;
        } else if (BinaryByteUnit.BYTES.toTebibytes(byteCount) > 0) {
            return BinaryByteUnit.TEBIBYTES;
        } else if (BinaryByteUnit.BYTES.toGibibytes(byteCount) > 0) {
            return BinaryByteUnit.GIBIBYTES;
        } else if (BinaryByteUnit.BYTES.toMebibytes(byteCount) > 0) {
            return BinaryByteUnit.MEBIBYTES;
        } else if (BinaryByteUnit.BYTES.toKibibytes(byteCount) > 0) {
            return BinaryByteUnit.KIBIBYTES;
        } else {
            return BinaryByteUnit.BYTES;
        }
    }

    /**
     * Selects a decimal byte unit based on the number of bytes.
     */
    private static DecimalByteUnit chooseDecimalByteUnit(long byteCount) {
        if (DecimalByteUnit.BYTES.toExabytes(byteCount) > 0) {
            return DecimalByteUnit.EXABYTES;
        } else if (DecimalByteUnit.BYTES.toPetabytes(byteCount) > 0) {
            return DecimalByteUnit.PETABYTES;
        } else if (DecimalByteUnit.BYTES.toTerabytes(byteCount) > 0) {
            return DecimalByteUnit.TERABYTES;
        } else if (DecimalByteUnit.BYTES.toGigabytes(byteCount) > 0) {
            return DecimalByteUnit.GIGABYTES;
        } else if (DecimalByteUnit.BYTES.toMegabytes(byteCount) > 0) {
            return DecimalByteUnit.MEGABYTES;
        } else if (DecimalByteUnit.BYTES.toKilobytes(byteCount) > 0) {
            return DecimalByteUnit.KILOBYTES;
        } else {
            return DecimalByteUnit.BYTES;
        }
    }

    /**
     * Selects a time unit based on the number of nanoseconds.
     */
    private static TimeUnit chooseTimeUnit(long nanos) {
        if (TimeUnit.NANOSECONDS.toDays(nanos) > 0) {
            return TimeUnit.DAYS;
        } else if (TimeUnit.NANOSECONDS.toHours(nanos) > 0) {
            return TimeUnit.HOURS;
        } else if (TimeUnit.NANOSECONDS.toMinutes(nanos) > 0) {
            return TimeUnit.MINUTES;
        } else if (TimeUnit.NANOSECONDS.toSeconds(nanos) > 0) {
            return TimeUnit.SECONDS;
        } else if (TimeUnit.NANOSECONDS.toMillis(nanos) > 0) {
            return TimeUnit.MILLISECONDS;
        } else if (TimeUnit.NANOSECONDS.toMicros(nanos) > 0) {
            return TimeUnit.MICROSECONDS;
        } else {
            return TimeUnit.NANOSECONDS;
        }
    }

}
