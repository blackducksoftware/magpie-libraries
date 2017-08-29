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
package com.blackducksoftware.common.base;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/**
 * Tests for {@code ExtraThrowables}.
 *
 * @author jgustie
 */
public class ExtraThrowablesTest {

    @Test
    public void format() {
        assertThat(ExtraThrowables.illegalArgument("foobar").get()).hasMessage("foobar");
        assertThat(ExtraThrowables.illegalState("%s", "foobar").get()).hasMessage("foobar");
        assertThat(ExtraThrowables.noSuchElement("%s%s", "foo", "bar").get()).hasMessage("foobar");
        assertThat(ExtraThrowables.nullPointer("%s%s%s", "fo", 0, "bar").get()).hasMessage("fo0bar");
    }

}
