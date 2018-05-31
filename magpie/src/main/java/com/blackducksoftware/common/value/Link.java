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

import static com.blackducksoftware.common.value.Rules.TokenType.RFC5988;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * A link as described in RFC5988. Each link consists of a URI reference and zero or more parameters.
 *
 * @author jgustie
 * @see <a href="https://tools.ietf.org/html/rfc5988">Web Linking</a>
 */
public class Link {

    private final String uriReference;

    // TODO ListMultimap?
    private final ImmutableMap<String, String> linkParams;

    private Link(Builder builder) {
        uriReference = Objects.requireNonNull(builder.uriReference);
        linkParams = ImmutableMap.copyOf(Maps.filterValues(builder.linkParams, Objects::nonNull));
    }

    public String uriReference() {
        return uriReference;
    }

    public Optional<String> linkParam(String parmname) {
        return Optional.ofNullable(linkParams.get(parmname));
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
        for (Map.Entry<String, String> linkParam : linkParams.entrySet()) {
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

    public static class Builder {

        private String uriReference;

        private Map<String, String> linkParams;

        public Builder() {
            // Re-insertion does not effect ordering, so establish a default stable order
            linkParams = new LinkedHashMap<>();
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
            linkParams = new LinkedHashMap<>(link.linkParams);
        }

        public Builder uriReference(CharSequence uriReference) {
            this.uriReference = uriReference.toString(); // TODO Validate?
            return this;
        }

        public Builder linkParam(CharSequence parmname, CharSequence value) {
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
