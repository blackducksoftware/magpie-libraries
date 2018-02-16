/*
 * Copyright 2016 Black Duck Software, Inc.
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
import static com.google.common.truth.Truth8.assertThat;

import java.util.BitSet;
import java.util.EnumSet;

import org.junit.Test;

/**
 * Tests for {@code ExtraEnums}.
 *
 * @author jgustie
 */
public class ExtraEnumsTest {

    public enum TestEnum {
        ENUM_0, ENUM_1, ENUM_2;

        @Override
        public String toString() {
            return String.valueOf(ordinal());
        }
    }

    public enum TestJumboEnum {
        ENUM_00, ENUM_01, ENUM_02, ENUM_03, ENUM_04, ENUM_05, ENUM_06, ENUM_07,
        ENUM_08, ENUM_09, ENUM_0A, ENUM_0B, ENUM_0C, ENUM_0D, ENUM_0E, ENUM_0F,
        ENUM_10, ENUM_11, ENUM_12, ENUM_13, ENUM_14, ENUM_15, ENUM_16, ENUM_17,
        ENUM_18, ENUM_19, ENUM_1A, ENUM_1B, ENUM_1C, ENUM_1D, ENUM_1E, ENUM_1F,
        ENUM_20, ENUM_21, ENUM_22, ENUM_23, ENUM_24, ENUM_25, ENUM_26, ENUM_27,
        ENUM_28, ENUM_29, ENUM_2A, ENUM_2B, ENUM_2C, ENUM_2D, ENUM_2E, ENUM_2F,
        ENUM_30, ENUM_31, ENUM_32, ENUM_33, ENUM_34, ENUM_35, ENUM_36, ENUM_37,
        ENUM_38, ENUM_39, ENUM_3A, ENUM_3B, ENUM_3C, ENUM_3D, ENUM_3E, ENUM_3F,
        ENUM_40, ENUM_41, ENUM_42, ENUM_43, ENUM_44, ENUM_45, ENUM_46, ENUM_47,
        ENUM_48, ENUM_49, ENUM_4A, ENUM_4B, ENUM_4C, ENUM_4D, ENUM_4E, ENUM_4F;
    }

    @Test
    public void tryByName_present() {
        assertThat(ExtraEnums.tryByName(TestEnum.class, "ENUM_0")).hasValue(TestEnum.ENUM_0);
    }

    @Test
    public void tryByName_empty() {
        assertThat(ExtraEnums.tryByName(TestEnum.class, "ENUM_3")).isEmpty();
    }

    @Test
    public void tryByToString_present() {
        assertThat(ExtraEnums.tryByToString(TestEnum.class, "0")).containsExactly(TestEnum.ENUM_0);
    }

    @Test
    public void tryByToString_empty() {
        assertThat(ExtraEnums.tryByToString(TestEnum.class, "3")).isEmpty();
    }

    @Test
    public void previous_first() {
        assertThat(ExtraEnums.previous(TestEnum.ENUM_0)).isEmpty();
    }

    @Test
    public void previous_middle() {
        assertThat(ExtraEnums.previous(TestEnum.ENUM_1)).hasValue(TestEnum.ENUM_0);
    }

    @Test
    public void previous_last() {
        assertThat(ExtraEnums.previous(TestEnum.ENUM_2)).hasValue(TestEnum.ENUM_1);
    }

    @Test
    public void next_first() {
        assertThat(ExtraEnums.next(TestEnum.ENUM_0)).hasValue(TestEnum.ENUM_1);
    }

    @Test
    public void next_middle() {
        assertThat(ExtraEnums.next(TestEnum.ENUM_1)).hasValue(TestEnum.ENUM_2);
    }

    @Test
    public void next_last() {
        assertThat(ExtraEnums.next(TestEnum.ENUM_2)).isEmpty();
    }

    @Test
    public void set_empty() {
        EnumSet<TestEnum> s = EnumSet.noneOf(TestEnum.class);
        assertThat(ExtraEnums.set(s, TestEnum.ENUM_0, false)).isFalse();
        assertThat(s).doesNotContain(TestEnum.ENUM_0);
        assertThat(ExtraEnums.set(s, TestEnum.ENUM_0, true)).isTrue();
        assertThat(s).contains(TestEnum.ENUM_0);
    }

    @Test
    public void set_full() {
        EnumSet<TestEnum> s = EnumSet.allOf(TestEnum.class);
        assertThat(ExtraEnums.set(s, TestEnum.ENUM_0, true)).isFalse();
        assertThat(s).contains(TestEnum.ENUM_0);
        assertThat(ExtraEnums.set(s, TestEnum.ENUM_0, false)).isTrue();
        assertThat(s).doesNotContain(TestEnum.ENUM_0);
    }

    @Test(expected = NullPointerException.class)
    public void set_nullSet() {
        ExtraEnums.set(null, TestEnum.ENUM_0, false);
    }

    @Test(expected = NullPointerException.class)
    public void set_nullValue() {
        ExtraEnums.set(EnumSet.noneOf(TestEnum.class), null, false);
    }

    @Test
    public void flip_absent() {
        EnumSet<TestEnum> s = EnumSet.noneOf(TestEnum.class);
        ExtraEnums.flip(s, TestEnum.ENUM_0);
        assertThat(s).contains(TestEnum.ENUM_0);
    }

    @Test
    public void flip_present() {
        EnumSet<TestEnum> s = EnumSet.allOf(TestEnum.class);
        ExtraEnums.flip(s, TestEnum.ENUM_0);
        assertThat(s).doesNotContain(TestEnum.ENUM_0);
    }

    @Test(expected = NullPointerException.class)
    public void flip_nullSet() {
        ExtraEnums.flip(null, TestEnum.ENUM_0);
    }

    @Test(expected = NullPointerException.class)
    public void flip_nullValue() {
        ExtraEnums.flip(EnumSet.noneOf(TestEnum.class), null);
    }

    @Test
    public void fromBitSet_long() {
        assertThat(ExtraEnums.fromBitSet(TestEnum.class, 0x05)).containsExactly(TestEnum.ENUM_0, TestEnum.ENUM_2);
    }

    @Test
    public void fromBitSet_jumboLong() {
        assertThat(ExtraEnums.fromBitSet(TestJumboEnum.class, Long.MIN_VALUE)).containsExactly(TestJumboEnum.ENUM_3F);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBitSet_longOverflowTestEnum() {
        ExtraEnums.fromBitSet(TestEnum.class, 0x08);
    }

    @Test
    public void fromBitSet_jumboBitSet() {
        assertThat(ExtraEnums.fromBitSet(TestJumboEnum.class, BitSet.valueOf(new long[] { 0x0D })))
                .containsExactly(TestJumboEnum.ENUM_00, TestJumboEnum.ENUM_02, TestJumboEnum.ENUM_03);
    }

    @Test
    public void fromBitSet_jumboBitSetForHumans() {
        BitSet bitSet = new BitSet();
        bitSet.set(TestJumboEnum.ENUM_00.ordinal());
        bitSet.set(TestJumboEnum.ENUM_3F.ordinal());
        bitSet.set(TestJumboEnum.ENUM_40.ordinal());
        bitSet.set(TestJumboEnum.ENUM_4F.ordinal());
        assertThat(ExtraEnums.fromBitSet(TestJumboEnum.class, bitSet))
                .containsExactly(TestJumboEnum.ENUM_00, TestJumboEnum.ENUM_3F, TestJumboEnum.ENUM_40, TestJumboEnum.ENUM_4F);

    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBitSet_bitSetOverflowTestJumboEnum() {
        ExtraEnums.fromBitSet(TestJumboEnum.class, BitSet.valueOf(new long[] { 0, 0x10000 }));
    }

}
