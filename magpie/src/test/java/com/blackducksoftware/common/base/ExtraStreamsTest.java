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

import static com.google.common.truth.Truth8.assertThat;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.junit.Test;

/**
 * Tests for {@code ExtraStreams}.
 *
 * @author jgustie
 */
public class ExtraStreamsTest {

    public enum TestEnum {
        ENUM_0, ENUM_1, ENUM_2
    }

    @Test(expected = NullPointerException.class)
    public void ofTypeNull() {
        ExtraStreams.ofType(null);
    }

    @Test
    public void ofTypeStreamNull() {
        String test = null;
        assertThat(Stream.of(test).flatMap(ExtraStreams.ofType(String.class))).isEmpty();
    }

    @Test
    public void ofTypeString() {
        Stream<Object> objects = Stream.of("a", Integer.valueOf(1), Long.valueOf(1));
        assertThat(objects.flatMap(ExtraStreams.ofType(String.class))).containsExactly("a");
    }

    @Test
    public void ofTypeNumber() {
        Stream<Object> objects = Stream.of("a", Integer.valueOf(1), Long.valueOf(1));
        assertThat(objects.flatMap(ExtraStreams.ofType(Number.class))).containsExactly(1, 1L);
    }

    @Test(expected = NullPointerException.class)
    public void fromOptionalNull() {
        ExtraStreams.fromOptional(null);
    }

    @Test
    public void fromOptionalEmpty() {
        assertThat(Stream.of("foo").flatMap(ExtraStreams.fromOptional(t -> Optional.empty()))).isEmpty();
    }

    @Test
    public void fromOptionalPresent() {
        assertThat(Stream.of("foo").flatMap(ExtraStreams.fromOptional(Optional::of))).containsExactly("foo");
    }

    @Test(expected = NullPointerException.class)
    public void streamEnumerationNull() {
        ExtraStreams.stream((Enumeration<?>) null);
    }

    @Test
    public void streamEnumeration() {
        assertThat(ExtraStreams.stream(new StringTokenizer("a b c"))).containsExactly("a", "b", "c").inOrder();
    }

    @Test(expected = NullPointerException.class)
    public void streamEnumNull() {
        ExtraStreams.stream((Class<TestEnum>) null);
    }

    @Test
    public void streamEnum() {
        assertThat(ExtraStreams.stream(TestEnum.class)).containsExactly(TestEnum.ENUM_0, TestEnum.ENUM_1, TestEnum.ENUM_2).inOrder();
    }

    @Test
    public void streamNullableNull() {
        assertThat(ExtraStreams.streamNullable(null)).isEmpty();
    }

    @Test
    public void streamNullableNonNull() {
        assertThat(ExtraStreams.streamNullable(Arrays.asList("test"))).containsExactly("test");
    }

    @Test
    public void ofNullableNull() {
        assertThat(ExtraStreams.ofNullable(null)).isEmpty();
    }

    @Test
    public void ofNullableNonNull() {
        assertThat(ExtraStreams.ofNullable("test")).containsExactly("test");
    }

}
