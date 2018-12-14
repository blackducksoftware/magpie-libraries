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
package com.blackducksoftware.common.io;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;

/**
 * A {@code BinaryByteUnit} represents a binary multiplier that is a power of 1024 and provides utility methods to
 * convert to other units.
 *
 * @author jgustie
 */
public enum BinaryByteUnit implements ByteUnit {

    // https://en.wikipedia.org/wiki/Binary_prefix
    // java.util.concurrent.TimeUnit

    /**
     * Byte unit representing one (1024<sup>0</sup>) byte.
     */
    BYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return c; }
        @Override public long toKibibytes(long c) { return c/(C1/C0); }
        @Override public long toMebibytes(long c) { return c/(C2/C0); }
        @Override public long toGibibytes(long c) { return c/(C3/C0); }
        @Override public long toTebibytes(long c) { return c/(C4/C0); }
        @Override public long toPebibytes(long c) { return c/(C5/C0); }
        @Override public long toExbibytes(long c) { return c/(C6/C0); }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toBytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing one thousand twenty four (1024<sup>1</sup>) bytes.
     */
    KIBIBYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return x(c, C1/C0, MAX_VALUE/(C1/C0)); }
        @Override public long toKibibytes(long c) { return c; }
        @Override public long toMebibytes(long c) { return c/(C2/C1); }
        @Override public long toGibibytes(long c) { return c/(C3/C1); }
        @Override public long toTebibytes(long c) { return c/(C4/C1); }
        @Override public long toPebibytes(long c) { return c/(C5/C1); }
        @Override public long toExbibytes(long c) { return c/(C6/C1); }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toKibibytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing one million forty eight thousand five hundred seventy size (1024<sup>2</sup>) bytes.
     */
    MEBIBYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return x(c, C2/C0, MAX_VALUE/(C2/C0)); }
        @Override public long toKibibytes(long c) { return x(c, C2/C1, MAX_VALUE/(C2/C1)); }
        @Override public long toMebibytes(long c) { return c; }
        @Override public long toGibibytes(long c) { return c/(C3/C2); }
        @Override public long toTebibytes(long c) { return c/(C4/C2); }
        @Override public long toPebibytes(long c) { return c/(C5/C2); }
        @Override public long toExbibytes(long c) { return c/(C6/C2); }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toMebibytes(c); }
    // @formatter:on
    },

    // Enough with the check writing...

    /**
     * Byte unit representing 1024<sup>3</sup> bytes.
     */
    GIBIBYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return x(c, C3/C0, MAX_VALUE/(C3/C0)); }
        @Override public long toKibibytes(long c) { return x(c, C3/C1, MAX_VALUE/(C3/C1)); }
        @Override public long toMebibytes(long c) { return x(c, C3/C2, MAX_VALUE/(C3/C2)); }
        @Override public long toGibibytes(long c) { return c; }
        @Override public long toTebibytes(long c) { return c/(C4/C3); }
        @Override public long toPebibytes(long c) { return c/(C5/C3); }
        @Override public long toExbibytes(long c) { return c/(C6/C3); }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toGibibytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing 1024<sup>4</sup> bytes.
     */
    TEBIBYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return x(c, C4/C0, MAX_VALUE/(C4/C0)); }
        @Override public long toKibibytes(long c) { return x(c, C4/C1, MAX_VALUE/(C4/C1)); }
        @Override public long toMebibytes(long c) { return x(c, C4/C2, MAX_VALUE/(C4/C2)); }
        @Override public long toGibibytes(long c) { return x(c, C4/C3, MAX_VALUE/(C4/C3)); }
        @Override public long toTebibytes(long c) { return c; }
        @Override public long toPebibytes(long c) { return c/(C5/C4); }
        @Override public long toExbibytes(long c) { return c/(C6/C4); }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toTebibytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing 1024<sup>5</sup> bytes.
     */
    PEBIBYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return x(c, C5/C0, MAX_VALUE/(C5/C0)); }
        @Override public long toKibibytes(long c) { return x(c, C5/C1, MAX_VALUE/(C5/C1)); }
        @Override public long toMebibytes(long c) { return x(c, C5/C2, MAX_VALUE/(C5/C2)); }
        @Override public long toGibibytes(long c) { return x(c, C5/C3, MAX_VALUE/(C5/C3)); }
        @Override public long toTebibytes(long c) { return x(c, C5/C4, MAX_VALUE/(C5/C4)); }
        @Override public long toPebibytes(long c) { return c; }
        @Override public long toExbibytes(long c) { return c/(C6/C5); }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toPebibytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing 1024<sup>6</sup> bytes.
     */
    EXBIBYTES {
    // @formatter:off
        @Override public long toBytes(long c)     { return x(c, C6/C0, MAX_VALUE/(C6/C0)); }
        @Override public long toKibibytes(long c) { return x(c, C6/C1, MAX_VALUE/(C6/C1)); }
        @Override public long toMebibytes(long c) { return x(c, C6/C2, MAX_VALUE/(C6/C2)); }
        @Override public long toGibibytes(long c) { return x(c, C6/C3, MAX_VALUE/(C6/C3)); }
        @Override public long toTebibytes(long c) { return x(c, C6/C4, MAX_VALUE/(C6/C4)); }
        @Override public long toPebibytes(long c) { return x(c, C6/C5, MAX_VALUE/(C6/C5)); }
        @Override public long toExbibytes(long c) { return c; }
        @Override public long convert(long c, BinaryByteUnit u) { return u.toExbibytes(c); }
    // @formatter:on
    },

    // Zebibytes and yobibytes need more then 64 bits

    ;

    // Constants for powers
    // TODO These names match the power of 1024, but should we use the prefix itself?
    // @formatter:off
    private static final long C0 = 1L;
    private static final long C1 = C0 * 1024L;
    private static final long C2 = C1 * 1024L;
    private static final long C3 = C2 * 1024L;
    private static final long C4 = C3 * 1024L;
    private static final long C5 = C4 * 1024L;
    private static final long C6 = C5 * 1024L;
    // @formatter:on

    // Safe multiply
    private static long x(long d, long m, long over) {
        if (d > over) {
            return MAX_VALUE;
        } else if (d < -over) {
            return MIN_VALUE;
        } else {
            return d * m;
        }
    }

    /**
     * Equivalent to {@link #convert(long, BinaryByteUnit) KIBIBYTES.convert(count, this)}.
     */
    public abstract long toKibibytes(long count);

    /**
     * Equivalent to {@link #convert(long, BinaryByteUnit) MEBIBYTES.convert(count, this)}.
     */
    public abstract long toMebibytes(long count);

    /**
     * Equivalent to {@link #convert(long, BinaryByteUnit) GIBIBYTES.convert(count, this)}.
     */
    public abstract long toGibibytes(long count);

    /**
     * Equivalent to {@link #convert(long, BinaryByteUnit) TEBIBYTES.convert(count, this)}.
     */
    public abstract long toTebibytes(long count);

    /**
     * Equivalent to {@link #convert(long, BinaryByteUnit) PEBIBYTES.convert(count, this)}.
     */
    public abstract long toPebibytes(long count);

    /**
     * Equivalent to {@link #convert(long, BinaryByteUnit) EXBIBYTES.convert(count, this)}.
     */
    public abstract long toExbibytes(long count);

    /**
     * Converts the given byte count in the given unit to this unit.
     */
    public abstract long convert(long sourceCount, BinaryByteUnit sourceUnit);

}
