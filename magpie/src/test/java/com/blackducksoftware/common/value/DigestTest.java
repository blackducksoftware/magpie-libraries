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

import org.junit.Test;

import com.google.common.hash.Hashing;

/**
 * Tests for {@code Digest}.
 *
 * @author jgustie
 */
public class DigestTest {

    @Test
    public void parseSha256() {
        Digest digest = Digest.parse("sha256:" + Hashing.sha256().hashInt(0));
        assertThat(digest.algorithm()).isEqualTo("sha256");
        assertThat(digest.value()).isEqualTo(Hashing.sha256().hashInt(0).toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseMissingAlgorithm() {
        Digest.parse(":aaa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseMissingDelimiter() {
        Digest.parse("testaaa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseMissingValue() {
        Digest.parse("test:");
    }

}
