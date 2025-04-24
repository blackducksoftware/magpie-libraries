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
 * A {@code DecimalByteUnit} represents a binary multiplier that is a power of 1000 and provides utility methods to
 * convert to other units.
 *
 * @author jgustie
 */
public enum DecimalByteUnit implements ByteUnit {

    // https://en.wikipedia.org/wiki/Binary_prefix
    // java.util.concurrent.TimeUnit

    /**
     * Byte unit representing one (1000<sup>0</sup>) byte.
     */
    BYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return c; }
        @Override public long toKilobytes(long c)  { return c/(C1/C0); }
        @Override public long toMegabytes(long c)  { return c/(C2/C0); }
        @Override public long toGigabytes(long c)  { return c/(C3/C0); }
        @Override public long toTerabytes(long c)  { return c/(C4/C0); }
        @Override public long toPetabytes(long c)  { return c/(C5/C0); }
        @Override public long toExabytes(long c)   { return c/(C6/C0); }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toBytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing one thousand (1000<sup>1</sup>) bytes.
     */
    KILOBYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return safeMultiply(c, C1/C0, MAX_VALUE/(C1/C0)); }
        @Override public long toKilobytes(long c)  { return c; }
        @Override public long toMegabytes(long c)  { return c/(C2/C1); }
        @Override public long toGigabytes(long c)  { return c/(C3/C1); }
        @Override public long toTerabytes(long c)  { return c/(C4/C1); }
        @Override public long toPetabytes(long c)  { return c/(C5/C1); }
        @Override public long toExabytes(long c)   { return c/(C6/C1); }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toKilobytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing one million (1000<sup>2</sup>) bytes.
     */
    MEGABYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return safeMultiply(c, C2/C0, MAX_VALUE/(C2/C0)); }
        @Override public long toKilobytes(long c)  { return safeMultiply(c, C2/C1, MAX_VALUE/(C2/C1)); }
        @Override public long toMegabytes(long c)  { return c; }
        @Override public long toGigabytes(long c)  { return c/(C3/C2); }
        @Override public long toTerabytes(long c)  { return c/(C4/C2); }
        @Override public long toPetabytes(long c)  { return c/(C5/C2); }
        @Override public long toExabytes(long c)   { return c/(C6/C2); }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toMegabytes(c); }
    // @formatter:on
    },

    // Enough with the check writing...

    /**
     * Byte unit representing 1000<sup>3</sup>.
     */
    GIGABYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return safeMultiply(c, C3/C0, MAX_VALUE/(C3/C0)); }
        @Override public long toKilobytes(long c)  { return safeMultiply(c, C3/C1, MAX_VALUE/(C3/C1)); }
        @Override public long toMegabytes(long c)  { return safeMultiply(c, C3/C2, MAX_VALUE/(C3/C2)); }
        @Override public long toGigabytes(long c)  { return c; }
        @Override public long toTerabytes(long c)  { return c/(C4/C3); }
        @Override public long toPetabytes(long c)  { return c/(C5/C3); }
        @Override public long toExabytes(long c)   { return c/(C6/C3); }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toGigabytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing 1000<sup>4</sup>.
     */
    TERABYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return safeMultiply(c, C4/C0, MAX_VALUE/(C4/C0)); }
        @Override public long toKilobytes(long c)  { return safeMultiply(c, C4/C1, MAX_VALUE/(C4/C1)); }
        @Override public long toMegabytes(long c)  { return safeMultiply(c, C4/C2, MAX_VALUE/(C4/C2)); }
        @Override public long toGigabytes(long c)  { return safeMultiply(c, C4/C3, MAX_VALUE/(C4/C3)); }
        @Override public long toTerabytes(long c)  { return c; }
        @Override public long toPetabytes(long c)  { return c/(C5/C4); }
        @Override public long toExabytes(long c)   { return c/(C6/C4); }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toTerabytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing 1000<sup>5</sup>.
     */
    PETABYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return safeMultiply(c, C5/C0, MAX_VALUE/(C5/C0)); }
        @Override public long toKilobytes(long c)  { return safeMultiply(c, C5/C1, MAX_VALUE/(C5/C1)); }
        @Override public long toMegabytes(long c)  { return safeMultiply(c, C5/C2, MAX_VALUE/(C5/C2)); }
        @Override public long toGigabytes(long c)  { return safeMultiply(c, C5/C3, MAX_VALUE/(C5/C3)); }
        @Override public long toTerabytes(long c)  { return safeMultiply(c, C5/C4, MAX_VALUE/(C5/C4)); }
        @Override public long toPetabytes(long c)  { return c; }
        @Override public long toExabytes(long c)   { return c/(C6/C5); }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toPetabytes(c); }
    // @formatter:on
    },

    /**
     * Byte unit representing 1000<sup>6</sup>.
     */
    EXABYTES {
    // @formatter:off
        @Override public long toBytes(long c)      { return safeMultiply(c, C6/C0, MAX_VALUE/(C6/C0)); }
        @Override public long toKilobytes(long c)  { return safeMultiply(c, C6/C1, MAX_VALUE/(C6/C1)); }
        @Override public long toMegabytes(long c)  { return safeMultiply(c, C6/C2, MAX_VALUE/(C6/C2)); }
        @Override public long toGigabytes(long c)  { return safeMultiply(c, C6/C3, MAX_VALUE/(C6/C3)); }
        @Override public long toTerabytes(long c)  { return safeMultiply(c, C6/C4, MAX_VALUE/(C6/C4)); }
        @Override public long toPetabytes(long c)  { return safeMultiply(c, C6/C5, MAX_VALUE/(C6/C5)); }
        @Override public long toExabytes(long c)   { return c; }
        @Override public long convert(long c, DecimalByteUnit u) { return u.toExabytes(c); }
    // @formatter:on
    },

    // Zettabytes and yottabytes need more then 64 bits

    ;

    // Constants for powers
    // TODO These names match the power of 1000, but should we use the prefix itself?
    // @formatter:off
    private static final long C0 = 1L;
    private static final long C1 = C0 * 1000L;
    private static final long C2 = C1 * 1000L;
    private static final long C3 = C2 * 1000L;
    private static final long C4 = C3 * 1000L;
    private static final long C5 = C4 * 1000L;
    private static final long C6 = C5 * 1000L;
    // @formatter:on

    // Safe multiply
    private static long safeMultiply(long d, long m, long over) {
        if (d > over) {
            return MAX_VALUE;
        } else if (d < -over) {
            return MIN_VALUE;
        } else {
            return d * m;
        }
    }

    /**
     * Equivalent to {@link #convert(long, DecimalByteUnit) KILOBYTES.convert(count, this)}.
     */
    public abstract long toKilobytes(long count);

    /**
     * Equivalent to {@link #convert(long, DecimalByteUnit) MEGABYTES.convert(count, this)}.
     */
    public abstract long toMegabytes(long count);

    /**
     * Equivalent to {@link #convert(long, DecimalByteUnit) GIGABYTES.convert(count, this)}.
     */
    public abstract long toGigabytes(long count);

    /**
     * Equivalent to {@link #convert(long, DecimalByteUnit) TERABYTES.convert(count, this)}.
     */
    public abstract long toTerabytes(long count);

    /**
     * Equivalent to {@link #convert(long, DecimalByteUnit) PETABYTES.convert(count, this)}.
     */
    public abstract long toPetabytes(long count);

    /**
     * Equivalent to {@link #convert(long, DecimalByteUnit) EXABYTES.convert(count, this)}.
     */
    public abstract long toExabytes(long count);

    /**
     * Converts the given byte count in the given unit to this unit.
     */
    public abstract long convert(long sourceCount, DecimalByteUnit sourceUnit);

}
