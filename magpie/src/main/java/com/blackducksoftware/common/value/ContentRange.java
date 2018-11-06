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
package com.blackducksoftware.common.value;

import static com.blackducksoftware.common.base.ExtraThrowables.illegalArgument;
import static com.blackducksoftware.common.value.Rules.TokenType.RFC7230;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A content range as described in RFC7233. Each content range consists of a unit and a range. For some units, such as
 * "bytes", additional semantic interpretation of the range may be available.
 *
 * @author jgustie
 */
public abstract class ContentRange {

    public static final class ByteContentRange extends ContentRange {

        private final long firstBytePos;

        private final long lastBytePos;

        private final long completeLength;

        private ByteContentRange(Builder builder) {
            super(builder);
            checkByteRange(builder.firstByte, builder.lastByte, builder.contentLength);
            firstBytePos = builder.firstByte;
            lastBytePos = builder.lastByte;
            completeLength = builder.contentLength;
        }

        private static void checkByteRange(long firstByte, long lastByte, long contentLength) {
            if (firstByte < 0 && lastByte < 0) {
                checkArgument(contentLength >= 0, "unsatisfied range must have content-length");
            } else {
                checkArgument(firstByte <= lastByte, "invalid byte-range: %s-%s", firstByte, lastByte);
                checkArgument(contentLength < 0 || contentLength > lastByte, "invalid byte-range: %s-%s/%s", firstByte, lastByte, contentLength);
            }
        }

        @Override
        public String unit() {
            return "bytes";
        }

        @Override
        public String range() {
            if (isUnsatisfied()) {
                return "*/" + completeLength;
            } else {
                return firstBytePos + "-" + lastBytePos + '/' + (completeLength < 0 ? '*' : completeLength);
            }
        }

        public boolean isUnknownLength() {
            return completeLength < 0;
        }

        public boolean isUnsatisfied() {
            return firstBytePos < 0 && lastBytePos < 0 && !isUnknownLength();
        }

        public long firstBytePos() {
            return firstBytePos;
        }

        public long lastBytePos() {
            return lastBytePos;
        }

        public long completeLength() {
            return completeLength;
        }
    }

    public static final class OtherContentRange extends ContentRange {
        private final String unit;

        private final String range;

        private OtherContentRange(Builder builder) {
            super(builder);
            unit = Objects.requireNonNull(builder.unit);
            range = Objects.requireNonNull(builder.otherRange);
        }

        @Override
        public String unit() {
            return unit;
        }

        @Override
        public String range() {
            return range;
        }
    }

    private ContentRange(Builder builder) {
    }

    public abstract String unit();

    public abstract String range();

    @Override
    public int hashCode() {
        return Objects.hash(unit(), range());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentRange) {
            ContentRange other = (ContentRange) obj;
            return unit().equals(other.unit()) && range().equals(other.range());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return unit() + ' ' + range();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Synonym for {@code parse}.
     *
     * @see #parse(CharSequence)
     */
    public static ContentRange valueOf(String input) {
        return parse(input);
    }

    public static ContentRange parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static Optional<ContentRange> tryFrom(@Nullable Object obj) {
        if (obj instanceof ContentRange) {
            return Optional.of((ContentRange) obj);
        } else if (obj instanceof CharSequence) {
            return Optional.of(parse((CharSequence) obj));
        } else {
            return Optional.empty();
        }
    }

    public static ContentRange from(Object obj) {
        return tryFrom(Objects.requireNonNull(obj))
                .orElseThrow(illegalArgument("unexpected input: %s", obj));
    }

    public static class Builder {

        private String unit;

        private String otherRange;

        private long firstByte;

        private long lastByte;

        private long contentLength;

        public Builder() {
            unit = "bytes";
            firstByte = -1L;
            lastByte = -1L;
            contentLength = -1L;
        }

        private Builder(ContentRange contentRange) {
            unit = contentRange.unit();
            if (contentRange instanceof ByteContentRange) {
                ByteContentRange byteRange = (ByteContentRange) contentRange;
                firstByte = byteRange.firstBytePos;
                lastByte = byteRange.lastBytePos;
                contentLength = byteRange.completeLength;
            } else {
                otherRange = contentRange.range();
            }
        }

        public Builder byteRange(long firstByte, long lastByte, long contentLength) {
            checkArgument(firstByte >= 0, "invalid first-byte-pos: %s", firstByte);
            checkArgument(lastByte >= firstByte, "invalid last-byte-pos: %s", lastByte);
            checkArgument(contentLength > lastByte, "invalid content-length: %s", contentLength);
            this.firstByte = firstByte;
            this.lastByte = lastByte;
            this.contentLength = contentLength;
            return unit("bytes");
        }

        public Builder unsatisifiedByteRange(long contentLength) {
            checkArgument(contentLength >= 0, "invalid content-length: %s", contentLength);
            this.firstByte = -1L;
            this.lastByte = -1L;
            this.contentLength = contentLength;
            return unit("bytes");
        }

        public Builder unknownLengthByteRange(long firstByte, long lastByte) {
            checkArgument(firstByte >= 0, "invalid first-byte-pos: %s", firstByte);
            checkArgument(lastByte >= firstByte, "invalid last-byte-pos: %s", lastByte);
            this.firstByte = firstByte;
            this.lastByte = lastByte;
            this.contentLength = -1L;
            return unit("bytes");
        }

        public Builder otherRange(String unit, String range) {
            this.otherRange = range; // No restrictions?!
            return unit(unit);
        }

        public ContentRange build() {
            if (Rules.isBytesUnit(unit)) {
                return new ByteContentRange(this);
            } else {
                return new OtherContentRange(this);
            }
        }

        private Builder unit(CharSequence unit) {
            if (Rules.isBytesUnit(unit)) {
                this.unit = unit.toString();
            } else {
                this.unit = Rules.checkOtherRangeUnit(unit);
            }
            return this;
        }

        void parse(CharSequence input) {
            int start, end = 0;

            start = end;
            end = Rules.nextToken(RFC7230, input, start);
            checkArgument(end > start, "missing unit: %s", input);
            unit(input.subSequence(start, end));

            start = end;
            end = Rules.nextChar(input, start, ' ');
            checkArgument(end > start, "missing delimiter: %s", input);

            int length = input.length();
            if (Rules.isBytesUnit(unit)) {
                start = end;
                end = nextUnknown(input, start, "byte-range-res");
                if (end > start) {
                    firstByte = lastByte = -1L;
                } else {
                    end = Rules.nextDigit(input, start);
                    checkArgument(end > start, "missing first-byte-pos: %s", input);
                    firstByte = Long.parseLong(input.subSequence(start, end).toString());

                    start = end;
                    end = Rules.nextChar(input, start, '-');
                    checkArgument(end > start, "missing byte-range delimiter: %s", input);

                    start = end;
                    end = Rules.nextDigit(input, start);
                    checkArgument(end > start, "missing last-byte-pos: %s", input);
                    lastByte = Long.parseLong(input.subSequence(start, end).toString());
                }

                start = end;
                end = Rules.nextChar(input, start, '/');
                checkArgument(end > start, "missing byte-range-resp delimiter: %s", input);

                start = end;
                end = nextUnknown(input, start, "complete-length");
                if (end > start) {
                    contentLength = -1;
                } else {
                    end = Rules.nextDigit(input, start);
                    checkArgument(end > start, "missing content-length: %s", input);
                    contentLength = Long.parseLong(input.subSequence(start, end).toString());
                }
            } else {
                start = end;
                end = length;
                otherRange = input.subSequence(start, end).toString();
            }
        }

        private static int nextUnknown(CharSequence input, int start, String rule) {
            checkArgument(start < input.length(), "missing %s: %s", rule, input);
            return input.charAt(start) == '*' ? start + 1 : start;
        }

    }

}
