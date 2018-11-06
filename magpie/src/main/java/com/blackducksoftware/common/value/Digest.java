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
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.base.Ascii;
import com.google.common.base.CharMatcher;

/**
 * An arbitrary digest consisting of an algorithm identifier and the digest value.
 *
 * @author jgustie
 */
public class Digest {

    // This was originally named "Fingerprint"

    private final String algorithm;

    // TODO Change this to byte[]?
    private final String value;

    private Digest(Builder builder) {
        algorithm = Ascii.toLowerCase(builder.algorithm);
        value = Objects.requireNonNull(builder.value);
    }

    public String algorithm() {
        return algorithm;
    }

    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Digest) {
            Digest other = (Digest) obj;
            return algorithm.equals(other.algorithm) && value.equals(other.value);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return algorithm + ':' + value;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static Digest of(CharSequence algorithm, CharSequence value) {
        return new Builder().algorithm(algorithm).value(value).build();
    }

    /**
     * Synonym for {@code parse}.
     *
     * @see #parse(CharSequence)
     */
    public static Digest valueOf(String input) {
        return parse(input);
    }

    public static Digest parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static Optional<Digest> tryFrom(@Nullable Object obj) {
        if (obj instanceof Digest) {
            return Optional.of((Digest) obj);
        } else if (obj instanceof CharSequence) {
            return Optional.of(parse((CharSequence) obj));
        } else {
            return Optional.empty();
        }
    }

    public static Digest from(Object obj) {
        return tryFrom(Objects.requireNonNull(obj))
                .orElseThrow(illegalArgument("unexpected input: %s", obj));
    }

    public static class Builder {

        private static final CharMatcher ALGORITHM_CHARS = CharMatcher.inRange('A', 'Z')
                .or(CharMatcher.inRange('a', 'z'))
                .or(CharMatcher.inRange('0', '9'))
                .or(CharMatcher.anyOf("_+.-"));

        private String algorithm;

        private String value;

        public Builder() {
        }

        private Builder(Digest digest) {
            algorithm = digest.algorithm;
            value = digest.value;
        }

        public Builder algorithm(@Nullable CharSequence algorithm) {
            this.algorithm = algorithm != null ? checkAlgorithm(algorithm) : null;
            return this;
        }

        public Builder value(@Nullable CharSequence value) {
            this.value = value != null ? checkValue(value) : null;
            return this;
        }

        public Digest build() {
            return new Digest(this);
        }

        void parse(CharSequence input) {
            int start, end = 0;

            start = end;
            end = Rules.next(input, ALGORITHM_CHARS, start);
            checkArgument(end > start, "missing algorithm: %s", input);
            algorithm(input.subSequence(start, end));

            start = end;
            end = Rules.nextChar(input, start, ':');
            checkArgument(end > start, "missing delimiter: %s", input);

            start = end;
            end = input.length();
            value(input.subSequence(start, end));
        }

        private static String checkAlgorithm(@Nullable CharSequence input) {
            checkArgument(input != null, "null algorithm");
            checkArgument(input.length() > 0 && ALGORITHM_CHARS.matchesAllOf(input), "invalid algorithm: %s", input);
            return input.toString();
        }

        private static String checkValue(@Nullable CharSequence input) {
            checkArgument(input != null, "null value");
            // TODO Limit this to hex digits?
            checkArgument(input.length() > 0, "invalid value: <empty>");
            return input.toString();
        }

    }

}
