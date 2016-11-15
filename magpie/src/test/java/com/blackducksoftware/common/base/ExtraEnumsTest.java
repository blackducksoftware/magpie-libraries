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

import org.junit.Test;

import com.google.common.collect.ImmutableList;

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

    public enum TestNonuniqueEnum {
        ENUM_0, ENUM_1, ENUM_2;

        @Override
        public String toString() {
            return this == ENUM_0 ? "0" : "1+";
        }
    }

    @Test
    public void stringValues_all() {
        assertThat(ExtraEnums.stringValues(TestEnum.class)).containsExactly("0", "1", "2").inOrder();
    }

    @Test
    public void stringValues_iterable() {
        assertThat(ExtraEnums.stringValues(ImmutableList.of(TestEnum.ENUM_1, TestEnum.ENUM_0))).containsExactly("1", "0").inOrder();
    }

    @Test
    public void stringValues_some() {
        assertThat(ExtraEnums.stringValues(TestEnum.ENUM_1, TestEnum.ENUM_2)).containsExactly("1", "2").inOrder();
    }

    @Test
    public void uniqueStringValues_all() {
        assertThat(ExtraEnums.uniqueStringValues(TestNonuniqueEnum.class)).containsExactly("0", "1+");
    }

    @Test
    public void uniqueStringValues_iterable() {
        assertThat(ExtraEnums.uniqueStringValues(ImmutableList.of(TestNonuniqueEnum.ENUM_1, TestNonuniqueEnum.ENUM_0))).containsExactly("1+", "0");
    }

    @Test
    public void uniqueStringValues_some() {
        assertThat(ExtraEnums.uniqueStringValues(TestNonuniqueEnum.ENUM_1, TestNonuniqueEnum.ENUM_2)).containsExactly("1+");
    }

    @Test
    public void names_all() {
        assertThat(ExtraEnums.names(TestEnum.class)).containsExactly("ENUM_0", "ENUM_1", "ENUM_2");
    }

    @Test
    public void names_iterable() {
        assertThat(ExtraEnums.names(ImmutableList.of(TestEnum.ENUM_1, TestEnum.ENUM_0))).containsExactly("ENUM_1", "ENUM_0");
    }

    @Test
    public void names_some() {
        assertThat(ExtraEnums.names(TestEnum.ENUM_1, TestEnum.ENUM_2)).containsExactly("ENUM_1", "ENUM_2");
    }

}
