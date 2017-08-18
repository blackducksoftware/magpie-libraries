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

import static com.blackducksoftware.common.value.Rules.TokenType.RFC2045;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

/**
 * A content type as described in RFC2045, RFC2048, RFC4288 and RFC6838. Each content type consists of a top-level type,
 * a subtype and zero or more parameters.
 *
 * @author jgustie
 * @see <a href="https://tools.ietf.org/html/rfc2045">Multipurpose Internet Mail Extensions (MIME) Part One: Format of
 *      Internet Message Bodies</a>
 * @see <a href="https://tools.ietf.org/html/rfc2048">Multipurpose Internet Mail Extensions (MIME) Part Four:
 *      Registration Procedures</a>
 * @see <a href="https://tools.ietf.org/html/rfc4288">Media Type Specifications and Registration Procedures</a>
 * @see <a href="https://tools.ietf.org/html/rfc6838">Media Type Specifications and Registration Procedures</a>
 */
public class ContentType {

    // Calling this class "content type" instead of "media type" to be consistent with it's representation of a response
    // header describing an entity body.

    // TODO Special handling for facet and suffix a la. RFC6838?

    private final String type;

    private final String subtype;

    // TODO ListMultimap?
    private final ImmutableMap<String, String> parameters;

    private ContentType(Builder builder) {
        type = Objects.requireNonNull(builder.type);
        subtype = Objects.requireNonNull(builder.subtype);
        parameters = ImmutableMap.copyOf(builder.parameters);
    }

    public String type() {
        return type;
    }

    public String subtype() {
        return subtype;
    }

    public Optional<String> parameter(String attribute) {
        return Optional.ofNullable(parameters.get(attribute));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subtype, parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentType) {
            ContentType other = (ContentType) obj;
            // TODO Straight equivalence on the parameters is not acceptable
            return type.equalsIgnoreCase(other.type) && subtype.equalsIgnoreCase(other.subtype) && parameters.equals(other.parameters);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append(type).append('/').append(subtype);
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            result.append(';').append(parameter.getKey()).append('=').append(parameter.getValue());
        }
        return result.toString();
    }

    public static ContentType parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static class Builder {

        private String type;

        private String subtype;

        private Map<String, String> parameters;

        public Builder() {
            parameters = new LinkedHashMap<>();
        }

        public Builder type(CharSequence type) {
            this.type = Rules.checkType(type);
            return this;
        }

        public Builder subtype(CharSequence subtype) {
            this.subtype = Rules.checkSubtype(subtype);
            return this;
        }

        public Builder parameter(CharSequence attribute, CharSequence value) {
            parameters.put(Rules.checkAttribute(attribute), Rules.checkValue(value));
            return this;
        }

        public ContentType build() {
            return new ContentType(this);
        }

        void parse(CharSequence input) {
            int start, end = 0;
            parameters.clear();

            start = end;
            end = Rules.nextRegName(input, start);
            checkArgument(end > start, "missing type: %s", input);
            type(input.subSequence(start, end));

            start = end;
            end = Rules.nextChar(input, start, '/');
            checkArgument(end > start, "missing delimiter: %s", input);

            start = end;
            end = Rules.nextRegName(input, start);
            checkArgument(end > start, "missing subtype: %s", input);
            subtype(input.subSequence(start, end));

            start = end;
            end = Rules.remainingNameValues(RFC2045, input, start, this::parameter);
            checkArgument(end == input.length(), "invalid parameters: %s", input);
        }

    }

}
