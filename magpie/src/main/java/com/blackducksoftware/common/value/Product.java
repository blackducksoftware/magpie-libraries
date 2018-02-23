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

import static com.blackducksoftware.common.base.ExtraFunctions.curry;
import static com.blackducksoftware.common.base.ExtraOptionals.flatMapNullable;
import static com.blackducksoftware.common.value.Rules.TokenType.RFC7230;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * A product identifier as described in RFC7231. Each identifier consists of a required name, an optional version and
 * zero or more comments.
 *
 * @author jgustie
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">User-Agent</a>
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Server</a>
 */
public class Product {

    private final String name;

    private final Optional<String> version;

    private final ImmutableList<String> comments;

    private Product(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        version = Optional.ofNullable(builder.version);
        comments = ImmutableList.copyOf(builder.comments);
    }

    public String name() {
        return name;
    }

    @Nullable
    public String version() {
        return version.orElse(null);
    }

    public List<String> comments() {
        return comments;
    }

    @Override
    public int hashCode() {
        // Do not consider comments for equals/hashCode
        return Objects.hash(name, version);
    }

    @Override
    public boolean equals(Object obj) {
        // Do not consider comments for equals/hashCode
        if (obj instanceof Product) {
            Product other = (Product) obj;
            return name.equals(other.name) && version.equals(other.version);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append(name);
        if (version.isPresent()) {
            result.append('/').append(version.get());
        }
        for (String comment : comments) {
            result.append(' ').append(comment);
        }
        return result.toString();
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static Product parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static class Builder {

        private String name;

        private String version;

        private List<String> comments;

        public Builder() {
            comments = new ArrayList<>(1);
        }

        private Builder(Product product) {
            name = product.name;
            version = product.version.orElse(null);
            comments = new ArrayList<>(product.comments);
        }

        public Builder name(CharSequence name) {
            this.name = Rules.checkToken(name);
            return this;
        }

        public Builder version(@Nullable CharSequence version) {
            this.version = version != null ? Rules.checkToken(version) : null;
            return this;
        }

        public Builder comment(CharSequence comment) {
            comments = new ArrayList<>(Collections.singleton(Rules.checkComment(comment)));
            return this;
        }

        public Builder addComment(CharSequence comment) {
            comments.add(Rules.checkComment(comment));
            return this;
        }

        /**
         * Adds comment text. Using this method there is no need to wrap the comment in "()".
         */
        public Builder addCommentText(CharSequence comment, Object... args) {
            return addComment(String.format("(" + comment + ")", args));
        }

        public Builder simpleName(Class<?> type) {
            return name(type.getSimpleName());
        }

        public Builder implementationTitle(Class<?> type) {
            Optional<Package> pkg = Optional.ofNullable(type.getPackage());
            return name(flatMapNullable(pkg, Package::getImplementationTitle)
                    .map(curry(RFC7230, Rules::retainTokenChars))
                    .orElse(type.getSimpleName()));
        }

        public Builder implementationVersion(Class<?> type) {
            Optional<Package> pkg = Optional.ofNullable(type.getPackage());
            return version(flatMapNullable(pkg, Package::getImplementationVersion)
                    .map(curry(RFC7230, Rules::retainTokenChars))
                    .orElse(null));
        }

        public Product build() {
            checkState(name != null, "product name is required");
            return new Product(this);
        }

        void parse(CharSequence input) {
            int start, end = 0;
            version(null);
            comments.clear();

            start = end;
            end = Rules.nextToken(RFC7230, input, start);
            checkArgument(end > start, "missing name: %s", input);
            name(input.subSequence(start, end));

            start = end;
            if (start < input.length() && input.charAt(start++) == '/') {
                end = Rules.nextToken(RFC7230, input, start);
                checkArgument(end > start, "missing version: %s", input);
                version(input.subSequence(start, end));
            }

            start = end;
            if (start < input.length()) {
                end = Rules.nextNonWsp(input, start);
                checkArgument(end > start, "missing RWS: %s", input);
            }

            start = end;
            end = Rules.remainingTokens(input, start, (this::addComment));
            checkArgument(end == input.length(), "invalid comments: %s", input);
        }

    }

}
