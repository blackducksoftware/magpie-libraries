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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@code Link}.
 *
 * @author jgustie
 */
public class LinkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void absoluteUriOnly() {
        Link link = Link.parse("<http://example.com/>");
        assertThat(link.uriReference()).isEqualTo("http://example.com/");
    }

    @Test
    public void relativeUriOnly() {
        Link link = Link.parse("</test>");
        assertThat(link.uriReference()).isEqualTo("/test");
    }

    @Test
    public void missingUri() {
        thrown.expect(IllegalArgumentException.class);
        Link.parse("; rel=about");
    }

    @Test
    public void emptyUri() {
        thrown.expect(IllegalArgumentException.class);
        Link.parse("<>");
    }

    @Test
    public void missingUriEnd() {
        thrown.expect(IllegalArgumentException.class);
        Link.parse("</");
    }

    @Test
    public void uriAndRel() {
        Link link = Link.parse("<http://example.com/>;rel=about");
        assertThat(link.uriReference()).isEqualTo("http://example.com/");
        assertThat(link.linkParam("rel")).hasValue("about");
    }

    @Test
    public void undocumentedWhitespace() {
        // I don't see whitespace in the ABNF, but this is straight from section 5.5 of the specification...
        Link link = Link.parse("<http://example.com/TheBook/chapter2>; rel=\"previous\"; title=\"previous chapter\"");
        assertThat(link.uriReference()).isEqualTo("http://example.com/TheBook/chapter2");
        assertThat(link.linkParam("rel")).hasValue("\"previous\"");
        assertThat(link.linkParam("title")).hasValue("\"previous chapter\"");
    }

}
