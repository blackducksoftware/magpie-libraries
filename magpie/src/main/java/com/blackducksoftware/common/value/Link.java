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

import static com.blackducksoftware.common.base.ExtraThrowables.illegalArgument;
import static com.blackducksoftware.common.value.Rules.TokenType.RFC5988;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;

/**
 * A link as described in RFC5988. Each link consists of a URI reference and zero or more parameters.
 *
 * @author jgustie
 * @see <a href="https://tools.ietf.org/html/rfc5988">Web Linking</a>
 */
public class Link {

    private final String uriReference;

    private final ImmutableListMultimap<String, String> linkParams;

    private Link(Builder builder) {
        uriReference = Objects.requireNonNull(builder.uriReference);
        linkParams = ImmutableListMultimap.copyOf(Multimaps.filterValues(builder.linkParams, Objects::nonNull));
    }

    /**
     * Returns the URI reference of this link.
     */
    public String uriReference() {
        return uriReference;
    }

    /**
     * Returns all of the values for the specified link parameter, never {@code null}.
     */
    public List<String> linkParams(String parmname) {
        return linkParams.get(parmname);
    }

    /**
     * Returns the first value of the specified link parameter if it exists, otherwise empty.
     */
    public Optional<String> linkParam(String parmname) {
        return linkParams(parmname).stream().findFirst();
    }

    /**
     * Returns the value of the "rel" link parameter.
     *
     * @throws NoSuchElementException
     *             if "rel" is not defined
     */
    public String rel() {
        return linkParam("rel").get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriReference, linkParams);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Link) {
            Link other = (Link) obj;
            return uriReference.equals(other.uriReference) && linkParams.equals(other.linkParams);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append('<').append(uriReference).append('>');
        for (Map.Entry<String, String> linkParam : linkParams.entries()) {
            result.append(';').append(linkParam.getKey()).append('=').append(linkParam.getValue());
        }
        return result.toString();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Synonym for {@code parse}.
     *
     * @see #parse(CharSequence)
     */
    public static Link valueOf(String input) {
        return parse(input);
    }

    public static Link parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static Optional<Link> tryFrom(Object obj) {
        if (obj instanceof Link) {
            return Optional.of((Link) obj);
        } else if (obj instanceof CharSequence) {
            return Optional.ofNullable(parse((CharSequence) obj));
        } else {
            throw new IllegalArgumentException("unexpected input: " + obj);
        }
    }

    public static Link from(Object obj) {
        return tryFrom(Objects.requireNonNull(obj))
                .orElseThrow(illegalArgument("unexpected input: %s", obj));
    }

    public static class Builder {

        private static final Escaper TITLE_ESCAPER = new PercentEscaper("", false);

        private String uriReference;

        private Multimap<String, String> linkParams;

        public Builder() {
            // Re-insertion does not effect ordering, so establish a default stable order
            linkParams = LinkedListMultimap.create();
            linkParams.put("rel", null);
            linkParams.put("anchor", null);
            linkParams.put("rev", null);
            linkParams.put("hreflang", null);
            linkParams.put("media", null);
            linkParams.put("title", null);
            linkParams.put("title*", null);
            linkParams.put("type", null);
        }

        private Builder(Link link) {
            uriReference = link.uriReference;
            linkParams = LinkedListMultimap.create(link.linkParams);
        }

        public Builder uriReference(CharSequence uriReference) {
            this.uriReference = uriReference.toString(); // TODO Validate?
            return this;
        }

        public Builder linkParam(CharSequence parmname, CharSequence value) {
            // TODO Only rev (which is deprecated), hreflang and exts allow duplicate values
            // There should be a private version of this that keeps the first one and public that keeps the last
            String actualValue;
            switch (parmname.toString()) {
            case "rel":
                actualValue = Rules.checkRelationTypes(value);
                break;
            case "anchor":
                actualValue = Rules.checkURIReference(value);
                break;
            case "rev":
                actualValue = Rules.checkRelationTypes(value);
                break;
            case "hreflang":
                actualValue = Rules.checkLanguageTag(value);
                break;
            case "media":
                actualValue = Rules.checkMediaDesc(value);
                break;
            case "title":
                actualValue = Rules.checkQuotedString(value);
                break;
            case "title*":
                actualValue = Rules.checkExtValue(value);
                break;
            case "type":
                actualValue = Rules.checkMediaTypeOrQuotedMt(value);
                break;
            default:
                Rules.checkLinkExtension(parmname, value);
                actualValue = value.toString();
            }
            linkParams.put(parmname.toString(), actualValue);
            return this;
        }

        /**
         * Should be used for URI formated relations only.
         *
         * @see #rel()
         */
        public Builder rel(String rel) {
            return linkParam("rel", rel);
        }

        /**
         * Selects from a list of available relations based on a capture of the IANA link relations registry.
         *
         * @see <a href="https://www.iana.org/assignments/link-relations/link-relations.xhtml">Link Relations</a>
         * @see RegisteredLinkRelations
         */
        public RegisteredLinkRelations rel() {
            return new RegisteredLinkRelations(this);
        }

        // TODO Handle quoting! What is the 8288 vs 5988 impact of quoted strings?

        public Builder anchor(String anchor) {
            return linkParam("anchor", anchor);
        }

        public Builder hreflang(String hreflang) {
            return linkParam("hreflang", hreflang);
        }

        public Builder media(String media) {
            return linkParam("media", media);
        }

        public Builder title(String title) {
            return linkParam("title", title);
        }

        public Builder title(String title, Locale locale) {
            return linkParam("title*", "UTF-8'" + locale.toLanguageTag() + "'" + TITLE_ESCAPER.escape(title));
        }

        public Builder type(String type) {
            return linkParam("type", type);
        }

        public Link build() {
            return new Link(this);
        }

        void parse(CharSequence input) {
            int start, end = 0;
            linkParams.clear();

            start = end;
            end = Rules.nextChar(input, start, '<');
            checkArgument(end > start, "missing start link: %s", input);

            start = end;
            end = nextUriReference(input, start);
            checkArgument(end > start, "missing link: %s", input);
            uriReference(input.subSequence(start, end));

            start = end;
            end = Rules.nextChar(input, start, '>');
            checkArgument(end > start, "missing end link: %s", input);

            start = end;
            end = Rules.remainingNameValues(RFC5988, input, start, this::linkParam);
            checkArgument(end == input.length(), "invalid link-params: %s", input);
        }

        private static int nextUriReference(CharSequence input, int start) {
            int pos = start, length = input.length();
            while (pos < length - 1) {
                if (input.charAt(++pos) == '>') {
                    return pos;
                }
            }
            return start;
        }

    }

    // TODO Should we have a link list just like with products? Links can be comma delimited...

}
