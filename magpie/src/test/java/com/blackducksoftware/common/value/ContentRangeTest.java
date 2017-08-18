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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.common.value.ContentRange.ByteContentRange;
import com.blackducksoftware.common.value.ContentRange.OtherContentRange;

/**
 * Tests for {@code ContentRange}.
 * 
 * @author jgustie
 */
public class ContentRangeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void parseFullByteRange() {
        ContentRange bytes = ContentRange.parse("bytes 1-2/3");
        assertThat(bytes).isInstanceOf(ByteContentRange.class);
        assertThat(((ByteContentRange) bytes).firstBytePos()).isEqualTo(1L);
        assertThat(((ByteContentRange) bytes).lastBytePos()).isEqualTo(2L);
        assertThat(((ByteContentRange) bytes).completeLength()).isEqualTo(3L);
    }

    @Test
    public void parseUnsatisfiedByteRange() {
        ContentRange bytes = ContentRange.parse("bytes */3");
        assertThat(bytes).isInstanceOf(ByteContentRange.class);
        assertThat(((ByteContentRange) bytes).isUnsatisfied()).isTrue();
        assertThat(((ByteContentRange) bytes).completeLength()).isEqualTo(3L);
    }

    @Test
    public void parseUnknownLengthByteRange() {
        ContentRange bytes = ContentRange.parse("bytes 1-2/*");
        assertThat(bytes).isInstanceOf(ByteContentRange.class);
        assertThat(((ByteContentRange) bytes).isUnknownLength()).isTrue();
        assertThat(((ByteContentRange) bytes).firstBytePos()).isEqualTo(1L);
        assertThat(((ByteContentRange) bytes).lastBytePos()).isEqualTo(2L);
    }

    @Test
    public void parseMissingBytesRange() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse("bytes ");
    }

    @Test
    public void parseMissingFirstBytePos() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse("bytes -2/3");
    }

    @Test
    public void parseMissingLastBytePos() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse("bytes 1-/3");
    }

    @Test
    public void parseMissingCompleteLength() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse("bytes 1-2/");
    }

    @Test
    public void parseOtherRange() {
        ContentRange other = ContentRange.parse("test foobar");
        assertThat(other).isInstanceOf(OtherContentRange.class);
        assertThat(other.unit()).isEqualTo("test");
        assertThat(other.range()).isEqualTo("foobar");
    }

    @Test
    public void parseOtherRangeEmpty() {
        ContentRange other = ContentRange.parse("test ");
        assertThat(other).isInstanceOf(OtherContentRange.class);
        assertThat(other.unit()).isEqualTo("test");
        assertThat(other.range()).isEmpty();
    }

    @Test
    public void parseMissingUnit() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse(" foobar");
    }

    @Test
    public void parseMissingRangeDelimiter() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse("test");
    }

    @Test
    public void parseMissingInvalidDelimiter() {
        thrown.expect(IllegalArgumentException.class);
        ContentRange.parse("test{foobar");
    }
}
