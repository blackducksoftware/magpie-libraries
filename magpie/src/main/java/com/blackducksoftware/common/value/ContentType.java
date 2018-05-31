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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MoreCollectors;

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
    // header describing an entity body (e.g. no wildcards and such that you might find in an accept header)

    /**
     * Attribute name for the commonly used "charset" parameter.
     */
    private static final String CHARSET_ATTRIBUTE = "charset";

    private final String type;

    private final String subtype;

    private final ImmutableListMultimap<String, String> parameters;

    private ContentType(Builder builder) {
        type = Ascii.toLowerCase(builder.type);
        subtype = Ascii.toLowerCase(builder.subtype);

        ImmutableListMultimap.Builder<String, String> parameters = ImmutableListMultimap.builder();
        for (Map.Entry<String, String> parameter : builder.parameters.entries()) {
            String attribute = Ascii.toLowerCase(parameter.getKey());
            String value = parameter.getValue();

            // Normalize supported character set names (e.g. consider "utf8" and "UTF-8" as equivalent)
            if (attribute.equals(CHARSET_ATTRIBUTE) && Charset.isSupported(value)) {
                value = Charset.forName(value).name();
            }

            parameters.put(attribute, value);
        }
        this.parameters = parameters.build();
    }

    public String type() {
        return type;
    }

    public String subtype() {
        return subtype;
    }

    public List<String> parameter(String attribute) {
        return parameters.get(attribute);
    }

    /**
     * Returns the character set for this content type. If a content type defines a default character set and no charset
     * parameter is specified, the default is returned.
     * <p>
     * <em>IMPORTANT:</em> Some content type may have a resolvable character set but the content is actually encoded
     * using a different character specified using another mechanism defined by the standard that introduces the MIME
     * type registration. Use this value as a fallback to content type specific encoding rules.
     *
     * @throws UnsupportedOperationException
     *             If no charset parameter is defined and no default is known
     * @throws IllegalStateException
     *             If multiple charset parameters were defined
     */
    public Charset charset() {
        // NOTE: RFC 7159 (or 4627, or 6839, or 7158) does not define a "charset" parameter, it shouldn't be there
        if (is("application", "json") || hasSuffix("json")) {
            // RFC 7159 Section 8.1
            return StandardCharsets.UTF_8;
        }

        // Assume the content type supports the "charset" attribute
        return parameter(CHARSET_ATTRIBUTE).stream()
                .map(Charset::forName)
                .collect(MoreCollectors.toOptional())
                .orElseGet(() -> {
                    if (is("text", "plain")) {
                        // RFC 6657 Section 4
                        return StandardCharsets.US_ASCII;
                    } else if (is("application", "xml") || is("text", "xml")) {
                        // RFC 7303 Section 3
                        return StandardCharsets.UTF_8;
                    } else {
                        throw new UnsupportedOperationException("no charset available for: " + ContentType.this.toString());
                    }
                });
    }

    /**
     * Test to see if this content type has the specified suffix.
     */
    public boolean hasSuffix(CharSequence suffix) {
        int pos = subtype.length() - suffix.length();
        return pos > 0 && subtype.charAt(pos - 1) == '+'
                && Ascii.equalsIgnoreCase(subtype.subSequence(pos, subtype.length()), suffix);
    }

    /**
     * Test to see if this content type has the specified type and subtype. Comparison is case-insensitive.
     */
    public boolean is(CharSequence type, CharSequence subtype) {
        return Ascii.equalsIgnoreCase(this.type, type) && Ascii.equalsIgnoreCase(this.subtype, subtype);
    }

    /**
     * Test to see if this content type is another content type. This is a relaxed form of {@code equals} which only
     * requires that this content type's parameters be a subset instead of strictly equal.
     */
    public boolean is(ContentType other) {
        return is(other.type, other.subtype) && parameters.entries().containsAll(other.parameters.entries());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, subtype, parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContentType) {
            ContentType other = (ContentType) obj;
            return is(other.type, other.subtype) && parameters.equals(other.parameters);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append(type).append('/').append(subtype);
        for (Map.Entry<String, String> parameter : parameters.entries()) {
            result.append(';').append(parameter.getKey()).append('=').append(parameter.getValue());
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
    public static ContentType valueOf(String input) {
        return parse(input);
    }

    public static ContentType parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static class Builder {

        private String type;

        private String subtype;

        private ListMultimap<String, String> parameters;

        public Builder() {
            parameters = LinkedListMultimap.create();
        }

        private Builder(ContentType contentType) {
            type = contentType.type;
            subtype = contentType.subtype;
            parameters = LinkedListMultimap.create(contentType.parameters);
        }

        /**
         * Prefer one of the standards based top-level helpers.
         */
        public Builder type(CharSequence type) {
            this.type = Rules.checkType(type);
            return this;
        }

        /**
         * Prefer one of the tree specific subtype helpers.
         */
        public Builder subtype(CharSequence subtype) {
            this.subtype = Rules.checkSubtype(subtype);
            return this;
        }

        public Builder parameter(CharSequence attribute, CharSequence value) {
            parameters.put(Rules.checkAttribute(attribute), Rules.checkValue(value));
            return this;
        }

        public Builder text() {
            return type("text");
        }

        public Builder text(String charsetName) {
            return text().parameter(CHARSET_ATTRIBUTE, charsetName);
        }

        public Builder text(Charset charset) {
            return text(charset.name());
        }

        public Builder image() {
            return type("image");
        }

        public Builder audio() {
            return type("audio");
        }

        public Builder video() {
            return type("video");
        }

        public Builder application() {
            return type("application");
        }

        public Builder multipart(CharSequence boundary) {
            // RFC 2046 says that "unrecognized" subtypes are treated as "mixed", so offer that as a default
            return type("multipart").standard("mixed").parameter("boundary", boundary);
        }

        public Builder message() {
            return type("message");
        }

        public Builder font() {
            return type("font");
        }

        public Builder model() {
            return type("model");
        }

        public Builder standard(CharSequence subtype) {
            Objects.requireNonNull(subtype);
            return subtype(subtype);
        }

        public Builder standard(CharSequence subtype, CharSequence suffix) {
            Objects.requireNonNull(subtype);
            Objects.requireNonNull(suffix);
            return subtype(new StringBuilder().append(subtype).append('+').append(suffix));
        }

        public Builder subtype(CharSequence tree, CharSequence subtype) {
            Objects.requireNonNull(tree);
            Objects.requireNonNull(subtype);
            return subtype(new StringBuilder().append(tree).append('.').append(subtype));
        }

        public Builder subtype(CharSequence tree, CharSequence subtype, CharSequence suffix) {
            Objects.requireNonNull(tree);
            Objects.requireNonNull(subtype);
            Objects.requireNonNull(suffix);
            return subtype(new StringBuilder().append(tree).append('.').append(subtype).append('+').append(suffix));
        }

        public Builder vendor(CharSequence subtype) {
            return subtype("vnd", subtype);
        }

        public Builder vendor(CharSequence subtype, CharSequence suffix) {
            return subtype("vnd", subtype, suffix);
        }

        public Builder producer(CharSequence producer, CharSequence subtype) {
            return vendor(new StringBuilder().append(producer).append('.').append(subtype));
        }

        public Builder producer(CharSequence producer, CharSequence subtype, CharSequence suffix) {
            return vendor(new StringBuilder().append(producer).append('.').append(subtype), suffix);
        }

        public Builder personal(CharSequence subtype) {
            return subtype("prs", subtype);
        }

        public Builder personal(CharSequence subtype, CharSequence suffix) {
            return subtype("prs", subtype, suffix);
        }

        public Builder unregistered(CharSequence subtype) {
            return subtype("x", subtype);
        }

        public Builder unregistered(CharSequence subtype, CharSequence suffix) {
            return subtype("x", subtype, suffix);
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
