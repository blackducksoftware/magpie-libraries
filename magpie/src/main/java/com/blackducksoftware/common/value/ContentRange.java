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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;

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
                checkArgument(contentLength > lastByte, "invalid byte-range: %s-%s/%s", firstByte, lastByte, contentLength);
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

        public boolean isCompleteLengthUnknown() {
            return completeLength < 0;
        }

        public boolean isUnsatisfied() {
            return firstBytePos < 0 && lastBytePos < 0 && !isCompleteLengthUnknown();
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

    public static ContentRange parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
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

        public Builder byteRange(long firstByte, long lastByte, long contentLength) {
            checkArgument(firstByte >= 0, "invalid first-byte-pos: %s", firstByte);
            checkArgument(lastByte >= firstByte, "invalid last-byte-pos: %s", lastByte);
            checkArgument(contentLength > lastByte, "invalid content-length: %s", contentLength);
            this.unit = "bytes";
            this.firstByte = firstByte;
            this.lastByte = lastByte;
            this.contentLength = contentLength;
            return this;
        }

        public Builder unsatisifiedByteRange(long contentLength) {
            checkArgument(contentLength >= 0, "invalid content-length: %s", contentLength);
            this.unit = "bytes";
            this.firstByte = -1L;
            this.lastByte = -1L;
            this.contentLength = contentLength;
            return this;
        }

        public Builder unknownLengthByteRange(long firstByte, long lastByte) {
            checkArgument(firstByte >= 0, "invalid first-byte-pos: %s", firstByte);
            checkArgument(lastByte >= firstByte, "invalid last-byte-pos: %s", lastByte);
            this.unit = "bytes";
            this.firstByte = firstByte;
            this.lastByte = lastByte;
            this.contentLength = -1L;
            return this;
        }

        public Builder otherRange(String unit, String range) {
            this.unit = Rules.checkOtherRangeUnit(unit);
            this.otherRange = null;
            return this;
        }

        public ContentRange build() {
            if (Rules.isBytesUnit(unit)) {
                return new ByteContentRange(this);
            } else {
                return new OtherContentRange(this);
            }
        }

        void parse(CharSequence input) {
            // TODO
        }

    }

}
