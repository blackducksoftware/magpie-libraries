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

import static com.google.common.truth.Truth.assertThat;

import java.time.Duration;

import org.junit.Test;

import com.blackducksoftware.common.io.BinaryByteUnit;
import com.blackducksoftware.common.io.DecimalByteUnit;

/**
 * Tests for {@code ExtraFormats}.
 *
 * @author jgustie
 */
public class ExtraFormatsTest {

    @Test
    public void print_duration_toString() {
        assertThat(ExtraFormats.print(Duration.ofMillis(123)).toString()).isEqualTo("123.0 ms");
        assertThat(ExtraFormats.print(Duration.ofMillis(1234)).toString()).isEqualTo("1.234 s");
        assertThat(ExtraFormats.print(Duration.ofMillis(65432)).toString()).isEqualTo("1.091 min");
    }

    @Test
    public void print_duration_format() {
        assertThat(String.format("%s", ExtraFormats.print(Duration.ofMillis(123)))).isEqualTo("123.0 ms");
        assertThat(String.format("%10s", ExtraFormats.print(Duration.ofMillis(123)))).isEqualTo("  123.0 ms");
        assertThat(String.format("%.5s", ExtraFormats.print(Duration.ofMillis(123)))).isEqualTo("123.00 ms");
    }

    @Test
    public void print_byteCount_toString() {
        assertThat(ExtraFormats.print(1024, BinaryByteUnit.BYTES).toString()).isEqualTo("1.000 KiB");
        assertThat(ExtraFormats.print(1000, DecimalByteUnit.BYTES).toString()).isEqualTo("1.000 kB");
    }

    @Test
    public void print_byteCount_format() {
        assertThat(String.format("%s", ExtraFormats.print(1024, BinaryByteUnit.BYTES))).isEqualTo("1.000 KiB");
        assertThat(String.format("%10s", ExtraFormats.print(1024, BinaryByteUnit.BYTES))).isEqualTo(" 1.000 KiB");
        assertThat(String.format("%.5s", ExtraFormats.print(1024, BinaryByteUnit.BYTES))).isEqualTo("1.0000 KiB");
    }

    @Test
    public void print__byteCount_tooSmall() {
        assertThat(String.format("%4s", ExtraFormats.print(1024, BinaryByteUnit.BYTES))).isEqualTo("1.000 KiB");
    }

    @Test
    public void print_object_toString() {
        assertThat(ExtraFormats.print(() -> "abc").toString()).isEqualTo("abc");
    }

    @Test
    public void print_object_format() {
        assertThat(String.format("%s", ExtraFormats.print(() -> "abc"))).isEqualTo("abc");
        assertThat(String.format("%10s", ExtraFormats.print(() -> "abc"))).isEqualTo("       abc");
        assertThat(String.format("%.5s", ExtraFormats.print(() -> "abc"))).isEqualTo("abc");
    }

}
