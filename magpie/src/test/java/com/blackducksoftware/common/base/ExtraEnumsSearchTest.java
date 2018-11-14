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

import static com.blackducksoftware.common.base.ExtraEnums.search;
import static com.google.common.truth.Truth8.assertThat;

import org.junit.Test;

/**
 * Tests for the enum search functionality.
 *
 * @author jgustie
 */
public class ExtraEnumsSearchTest {

    public enum TestEnum {
        ENUM_0, enum_1, ENUM_VALUE_2, enumValue3;

        @Override
        public String toString() {
            return String.valueOf(ordinal());
        }
    }

    @Test
    public void search_byName() {
        assertThat(search(TestEnum.class).byName("ENUM_0")).hasValue(TestEnum.ENUM_0);
    }

    @Test
    public void search_byToString() {
        assertThat(search(TestEnum.class).byToString("0")).hasValue(TestEnum.ENUM_0);
    }

    @Test
    public void search_ignoringCase_byName() {
        // lower, MiXed, UPPER
        assertThat(search(TestEnum.class).ignoringCase().byName("enum_0")).hasValue(TestEnum.ENUM_0);
        assertThat(search(TestEnum.class).ignoringCase().byName("EnUm_0")).hasValue(TestEnum.ENUM_0);
        assertThat(search(TestEnum.class).ignoringCase().byName("ENUM_0")).hasValue(TestEnum.ENUM_0);

        // Works in both directions
        assertThat(search(TestEnum.class).ignoringCase().byName("ENUM_1")).hasValue(TestEnum.enum_1);
    }

    @Test
    public void search_ignoring_byName() {
        // Strip "_"
        assertThat(search(TestEnum.class).ignoring("_").byName("ENUM0")).hasValue(TestEnum.ENUM_0);

        // Works in both directions
        assertThat(search(TestEnum.class).ignoring("_").byName("ENUM_0")).hasValue(TestEnum.ENUM_0);
    }

    @Test
    public void search_usingLowerCamel_byName() {
        // UPPER_UNDERSCORE to lowerCamel
        assertThat(search(TestEnum.class).usingLowerCamel().byName("enumValue2")).hasValue(TestEnum.ENUM_VALUE_2);

        // Won't work since the constant is already lowerCamel
        assertThat(search(TestEnum.class).usingLowerCamel().byName("enumValue3")).isEmpty();
    }

    @Test
    public void search_startsWith_byName() {
        // Effectively adds the prefix to the search term
        assertThat(search(TestEnum.class).startsWith("ENUM_").byName("0")).hasValue(TestEnum.ENUM_0);

        // Allow the prefixed search term as well
        assertThat(search(TestEnum.class).startsWith("ENUM_").byName("ENUM_0")).hasValue(TestEnum.ENUM_0);
    }

    public enum TestAmbiguousEnum {
        ENUM_0, enum_0;

        @Override
        public String toString() {
            return "0";
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void search_byName_ambiguous() {
        search(TestAmbiguousEnum.class).ignoringCase().byName("ENUM_0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void search_byToString_ambiguous() {
        search(TestAmbiguousEnum.class).byToString("0");
    }

    @Test
    public void search_withName_ambiguous() {
        assertThat(search(TestAmbiguousEnum.class).ignoringCase().withName("ENUM_0"))
                .containsExactly(TestAmbiguousEnum.ENUM_0, TestAmbiguousEnum.enum_0).inOrder();
    }

    @Test
    public void search_withToString_ambiguous() {
        assertThat(search(TestAmbiguousEnum.class).withToString("0"))
                .containsExactly(TestAmbiguousEnum.ENUM_0, TestAmbiguousEnum.enum_0).inOrder();
    }

}
