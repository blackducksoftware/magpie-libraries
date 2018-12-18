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
package com.blackducksoftware.common.i18n;

import static com.google.common.base.Preconditions.checkArgument;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Objects;

import com.blackducksoftware.common.io.BinaryByteUnit;
import com.blackducksoftware.common.io.ByteUnit;
import com.blackducksoftware.common.io.DecimalByteUnit;

/**
 * A text format for units of data.
 *
 * @author jgustie
 */
public class BinaryPrefixFormat extends Format {

    private static final long serialVersionUID = 1L;

    public static final int NUMBER_FIELD = 0;

    public static final int PREFIX_FIELD = 1;

    /**
     * The style to use when abbreviating binary units.
     */
    public enum Style {
        /**
         * Binary prefixes are written in symbolic form.
         */
        SHORT,

        /**
         * Binary prefixes are written using their name.
         */
        LONG,
    }

    /**
     * A simple {@code ByteUnit} used internally.
     */
    private enum DefaultByteUnit implements ByteUnit {
        BYTES {
            @Override
            public long toBytes(long count) {
                return count;
            }

            @Override
            public long convert(long sourceCount, ByteUnit sourceUnit) {
                if (sourceUnit instanceof DefaultByteUnit) {
                    return ((DefaultByteUnit) sourceUnit).toBytes(sourceCount);
                } else if (sourceUnit instanceof BinaryByteUnit) {
                    return ((BinaryByteUnit) sourceUnit).toBytes(sourceCount);
                } else if (sourceUnit instanceof DecimalByteUnit) {
                    return ((DecimalByteUnit) sourceUnit).toBytes(sourceCount);
                } else {
                    throw new IllegalArgumentException("unknown byte unit: " + sourceUnit);
                }
            }
        };

        public abstract long convert(long sourceCount, ByteUnit sourceUnit);
    }

    public static BinaryPrefixFormat getInstance() {
        return getInstance(DecimalFormat.getNumberInstance());
    }

    public static BinaryPrefixFormat getInstance(NumberFormat numberFormat) {
        return getInstance(Style.SHORT, numberFormat);
    }

    public static BinaryPrefixFormat getInstance(Style style, NumberFormat numberFormat) {
        return new BinaryPrefixFormat(style, numberFormat);
    }

    public static BinaryPrefixFormat getBinaryInstance() {
        return getBinaryInstance(DecimalFormat.getNumberInstance());
    }

    public static BinaryPrefixFormat getBinaryInstance(NumberFormat numberFormat) {
        return getBinaryInstance(Style.SHORT, numberFormat);
    }

    public static BinaryPrefixFormat getBinaryInstance(Style style, NumberFormat numberFormat) {
        return new BinaryBinaryPrefixFormat(style, numberFormat);
    }

    public static BinaryPrefixFormat getDecimalInstance() {
        return getDecimalInstance(DecimalFormat.getNumberInstance());
    }

    public static BinaryPrefixFormat getDecimalInstance(NumberFormat numberFormat) {
        return new DecimalBinaryPrefixFormat(Style.SHORT, numberFormat);
    }

    public static BinaryPrefixFormat getDecimalInstance(Style style, NumberFormat numberFormat) {
        return new DecimalBinaryPrefixFormat(style, numberFormat);
    }

    /**
     * The style of this formatter.
     */
    private final Style style;

    /**
     * The number format used to format byte count.
     */
    private final NumberFormat numberFormat;

    protected BinaryPrefixFormat(Style style, NumberFormat numberFormat) {
        this.style = Objects.requireNonNull(style);
        this.numberFormat = Objects.requireNonNull(numberFormat);
    }

    @SuppressWarnings("JdkObsolete")
    public String format(long count, ByteUnit unit) {
        // TODO Use BigInteger to avoid topping out at Long.MAX_VALUE
        return format(unit.toBytes(count), new StringBuffer(), new FieldPosition(0)).toString();
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        checkArgument(obj instanceof Number, "Cannot format the give object as a byte count");
        // TODO A BigNumber can overflow here, need alternate logic
        Number number = (Number) obj;
        ByteUnit unit = chooseUnit(number.longValue());
        double value = number.doubleValue() / DefaultByteUnit.BYTES.convert(1, unit);
        // TODO How do we support FieldPosition correctly?
        return numberFormat.format(value, toAppendTo, pos).append(' ').append(abbreviate(unit, style));
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        // TODO Implement parsing
        return null;
    }

    protected ByteUnit chooseUnit(long count) {
        return DefaultByteUnit.BYTES;
    }

    protected String abbreviate(ByteUnit unit, Style style) {
        if (unit == DefaultByteUnit.BYTES && style == Style.SHORT) {
            return "B";
        } else if (style == Style.LONG) {
            return unit.toString().toLowerCase();
        } else {
            throw new UnsupportedOperationException("unable to abbreviate the unit " + unit + " using the style " + style);
        }
    }

    /**
     * Subclass to support binary (powers of 1024) units.
     */
    private static class BinaryBinaryPrefixFormat extends BinaryPrefixFormat {

        private static final long serialVersionUID = 1L;

        private static final String[] ABBREVIATIONS = { "B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB" };

        protected BinaryBinaryPrefixFormat(Style style, NumberFormat numberFormat) {
            super(style, numberFormat);
        }

        @Override
        protected BinaryByteUnit chooseUnit(long count) {
            if (BinaryByteUnit.BYTES.toExbibytes(count) > 0) {
                return BinaryByteUnit.EXBIBYTES;
            } else if (BinaryByteUnit.BYTES.toPebibytes(count) > 0) {
                return BinaryByteUnit.PEBIBYTES;
            } else if (BinaryByteUnit.BYTES.toTebibytes(count) > 0) {
                return BinaryByteUnit.TEBIBYTES;
            } else if (BinaryByteUnit.BYTES.toGibibytes(count) > 0) {
                return BinaryByteUnit.GIBIBYTES;
            } else if (BinaryByteUnit.BYTES.toMebibytes(count) > 0) {
                return BinaryByteUnit.MEBIBYTES;
            } else if (BinaryByteUnit.BYTES.toKibibytes(count) > 0) {
                return BinaryByteUnit.KIBIBYTES;
            } else {
                return BinaryByteUnit.BYTES;
            }
        }

        @Override
        protected String abbreviate(ByteUnit unit, Style style) {
            if (unit instanceof BinaryByteUnit && style == Style.SHORT) {
                return ABBREVIATIONS[((BinaryByteUnit) unit).ordinal()];
            } else {
                return super.abbreviate(unit, style);
            }
        }
    }

    /**
     * Subclass to support decimal (powers of 1000) units.
     */
    private static class DecimalBinaryPrefixFormat extends BinaryPrefixFormat {

        private static final long serialVersionUID = 1L;

        private static final String[] ABBREVIATIONS = { "B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };

        protected DecimalBinaryPrefixFormat(Style style, NumberFormat numberFormat) {
            super(style, numberFormat);
        }

        @Override
        protected DecimalByteUnit chooseUnit(long count) {
            if (DecimalByteUnit.BYTES.toExabytes(count) > 0) {
                return DecimalByteUnit.EXABYTES;
            } else if (DecimalByteUnit.BYTES.toPetabytes(count) > 0) {
                return DecimalByteUnit.PETABYTES;
            } else if (DecimalByteUnit.BYTES.toTerabytes(count) > 0) {
                return DecimalByteUnit.TERABYTES;
            } else if (DecimalByteUnit.BYTES.toGigabytes(count) > 0) {
                return DecimalByteUnit.GIGABYTES;
            } else if (DecimalByteUnit.BYTES.toMegabytes(count) > 0) {
                return DecimalByteUnit.MEGABYTES;
            } else if (DecimalByteUnit.BYTES.toKilobytes(count) > 0) {
                return DecimalByteUnit.KILOBYTES;
            } else {
                return DecimalByteUnit.BYTES;
            }
        }

        @Override
        protected String abbreviate(ByteUnit unit, Style style) {
            if (unit instanceof DecimalByteUnit && style == Style.SHORT) {
                return ABBREVIATIONS[((DecimalByteUnit) unit).ordinal()];
            } else {
                return super.abbreviate(unit, style);
            }
        }
    }

}
