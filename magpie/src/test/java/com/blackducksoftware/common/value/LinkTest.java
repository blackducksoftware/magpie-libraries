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

import org.junit.Ignore;
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
    public void optionalWhitespace() {
        Link link = Link.parse("<http://example.com/TheBook/chapter2>; rel=\"previous\"; title=\"previous chapter\"");
        assertThat(link.uriReference()).isEqualTo("http://example.com/TheBook/chapter2");
        assertThat(link.linkParam("rel")).hasValue("\"previous\"");
        assertThat(link.linkParam("title")).hasValue("\"previous chapter\"");
    }

    @Test
    public void linkParamOrder() {
        Link link = new Link.Builder().uriReference("/").linkParam("title", "\"FooBar\"").linkParam("anchor", "\"#gus\"").linkParam("hreflang", "en").build();
        assertThat(link.toString()).isEqualTo("</>;anchor=\"#gus\";hreflang=en;title=\"FooBar\"");
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc8288#section-3.5">Link Header Field Examples</a>
     */
    @Test
    public void example1() {
        Link example = Link.parse("<http://example.com/TheBook/chapter2>; rel=\"previous\";"
                + " title=\"previous chapter\"");
        assertThat(example.uriReference()).isEqualTo("http://example.com/TheBook/chapter2");
        assertThat(example.rel()).isEqualTo("\"previous\"");
        assertThat(example.linkParam("title")).hasValue("\"previous chapter\"");
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc8288#section-3.5">Link Header Field Examples</a>
     */
    @Test
    public void example2() {
        Link example = Link.parse("</>; rel=\"http://example.net/foo\"");
        assertThat(example.uriReference()).isEqualTo("/");
        assertThat(example.rel()).isEqualTo("\"http://example.net/foo\"");
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc8288#section-3.5">Link Header Field Examples</a>
     */
    @Test
    public void example3() {
        Link example = Link.parse("</terms>; rel=\"copyright\"; anchor=\"#foo\"");
        assertThat(example.uriReference()).isEqualTo("/terms");
        assertThat(example.rel()).isEqualTo("\"copyright\"");
        assertThat(example.linkParam("anchor")).hasValue("\"#foo\"");
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc8288#section-3.5">Link Header Field Examples</a>
     */
    @Test
    @Ignore
    public void example4() {
        // TODO We don't have multiple link support...
        Link example = Link.parse("</TheBook/chapter2>;"
                + " rel=\"previous\"; title*=UTF-8'de'letztes%20Kapitel,"
                + " </TheBook/chapter4>;"
                + " rel=\"next\"; title*=UTF-8'de'n%c3%a4chstes%20Kapitel");
        assertThat(example.uriReference()).isEqualTo("/TheBook/chapter2");
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc8288#section-3.5">Link Header Field Examples</a>
     */
    @Test
    @Ignore
    public void example5() {
        // TODO We don't properly split parameters
        Link example = Link.parse("<http://example.org/>;"
                + " rel=\"start http://example.net/relation/other\"");
        assertThat(example.uriReference()).isEqualTo("http://example.org/");
        assertThat(example.linkParams("rel")).containsExactly("start", "http://example.net/relation/other");
    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc8288#section-3.5">Link Header Field Examples</a>
     */
    @Test
    @Ignore
    public void example6() {
        // TODO We don't have multiple link support...
        Link example = Link.parse("<http://example.org/>; rel=\"start,"
                + " <http://example.org/index>; rel=\"index");
        assertThat(example.uriReference()).isEqualTo("http://example.org/");
    }

}
