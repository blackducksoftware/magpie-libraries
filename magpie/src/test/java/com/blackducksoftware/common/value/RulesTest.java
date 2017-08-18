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

public class RulesTest {

    @Test
    public void quotedStringEdgeCases() {
        assertThat(Rules.RFC7230.isQuotedString("")).isFalse();
        assertThat(Rules.RFC7230.isQuotedString("\"")).isFalse();
        assertThat(Rules.RFC7230.isQuotedString("\"\"")).isTrue();
        assertThat(Rules.RFC7230.isQuotedString("\"\"\"")).isFalse();
        assertThat(Rules.RFC7230.isQuotedString("\"\\\"")).isFalse();
        assertThat(Rules.RFC7230.isQuotedString("\"\\\"\"")).isTrue();
        assertThat(Rules.RFC7230.isQuotedString("\"\\\\\"")).isTrue();
        assertThat(Rules.RFC7230.isQuotedString("\"\\\\\\\"")).isFalse();
    }

    @Test
    public void commentEdgeCases() {
        assertThat(Rules.RFC7230.isComment("")).isFalse();
        assertThat(Rules.RFC7230.isComment("(")).isFalse();
        assertThat(Rules.RFC7230.isComment(")")).isFalse();
        assertThat(Rules.RFC7230.isComment("()")).isTrue();
        assertThat(Rules.RFC7230.isComment("(())")).isTrue();
        assertThat(Rules.RFC7230.isComment("(()())")).isTrue();
        assertThat(Rules.RFC7230.isComment("(()")).isFalse();

    }

}
