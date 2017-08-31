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
import static com.google.common.truth.Truth8.assertThat;

import org.junit.Test;

/**
 * Tests for {@code ContentType}.
 *
 * @author jgustie
 */
public class ContentTypeTest {

    /**
     * If you can't get this one right, go home.
     */
    @Test
    public void parseTextPlainWithCharset() {
        ContentType contentType = ContentType.parse("text/plain;charset=UTF-8");
        assertThat(contentType.type()).isEqualTo("text");
        assertThat(contentType.subtype()).isEqualTo("plain");
        assertThat(contentType.parameter("charset").findFirst()).hasValue("UTF-8");
    }

    @Test
    public void parseTextPlainWithCharsetAndWhitespace() {
        ContentType contentType = ContentType.parse("text/plain; charset=UTF-8");
        assertThat(contentType.type()).isEqualTo("text");
        assertThat(contentType.subtype()).isEqualTo("plain");
        assertThat(contentType.parameter("charset").findFirst()).hasValue("UTF-8");
    }

}
