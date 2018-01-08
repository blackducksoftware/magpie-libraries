/*
 * Copyright 2018 Black Duck Software, Inc.
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

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Tests for {@code ExtraCollectors}.
 *
 * @author jgustie
 */
public class ExtraCollectorsTest {

    public enum TestEnum {
        ENUM_0, ENUM_1, ENUM_2;

        @Override
        public String toString() {
            return String.valueOf(ordinal());
        }

        public static Stream<TestEnum> stream() {
            return ExtraStreams.stream(TestEnum.class);
        }
    }

    public enum TestNonuniqueEnum {
        ENUM_0, ENUM_1, ENUM_2;

        @Override
        public String toString() {
            return this == ENUM_0 ? "0" : "1+";
        }

        public static Stream<TestNonuniqueEnum> stream() {
            return ExtraStreams.stream(TestNonuniqueEnum.class);
        }
    }

    @Test
    public void enumStrings() {
        // TODO Eclipse bug keeps this from being a one-liner
        List<String> enumStrings = TestEnum.stream().collect(ExtraCollectors.enumStrings());
        assertThat(enumStrings).containsExactly("0", "1", "2").inOrder();
    }

    @Test
    public void uniqueEnumStrings() {
        // TODO Eclipse bug keeps this from being a one-liner
        Set<String> uniqueEnumStrings = TestNonuniqueEnum.stream().collect(ExtraCollectors.uniqueEnumStrings());
        assertThat(uniqueEnumStrings).containsExactly("0", "1+");
    }

    @Test
    public void enumNames() {
        // TODO Eclipse bug keeps this from being a one-liner
        Set<String> enumNames = TestEnum.stream().collect(ExtraCollectors.enumNames());
        assertThat(enumNames).containsExactly("ENUM_0", "ENUM_1", "ENUM_2");
    }

}
