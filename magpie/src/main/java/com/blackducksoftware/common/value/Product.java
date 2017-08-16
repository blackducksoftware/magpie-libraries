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
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2>Server</a>
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

        public Builder name(String name) {
            this.name = Rules.checkToken(name);
            return this;
        }

        public Builder version(String version) {
            this.version = Rules.checkToken(version);
            return this;
        }

        public Builder comment(String comment) {
            comments = new ArrayList<>(Collections.singleton(Rules.checkComment(comment)));
            return this;
        }

        public Builder addComment(String comment) {
            comments.add(Rules.checkComment(comment));
            return this;
        }

        public Product build() {
            checkState(name != null, "product name is required");
            return new Product(this);
        }

        void parse(CharSequence input) {
            // TODO
        }

    }

}
