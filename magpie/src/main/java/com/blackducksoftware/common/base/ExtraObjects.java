/*
 * Copyright 2016 Black Duck Software, Inc.
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
package com.blackducksoftware.common.base;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Extra object helpers.
 *
 * @author jgustie
 */
public final class ExtraObjects {

    /**
     * A helper for building {@code toString} representations.
     */
    public static final class ToStringBuilder<T> {

        /**
         * The object being represented.
         */
        private final T self;

        /**
         * The current representation. Initially {@literal null} so we know when to start appending delimiters.
         */
        private StringBuilder value;

        private ToStringBuilder(T self) {
            this.self = Objects.requireNonNull(self);
        }

        /**
         * Adds a boolean value to the current representation.
         */
        public ToStringBuilder<T> add(String name, boolean value) {
            nameEquals(name).append(value);
            return this;
        }

        /**
         * Adds a char value to the current representation.
         */
        public ToStringBuilder<T> add(String name, char value) {
            nameEquals(name).append(value);
            return this;
        }

        /**
         * Adds an int value to the current representation.
         */
        public ToStringBuilder<T> add(String name, int value) {
            nameEquals(name).append(value);
            return this;
        }

        /**
         * Adds a long value to the current representation.
         */
        public ToStringBuilder<T> add(String name, long value) {
            nameEquals(name).append(value);
            return this;
        }

        /**
         * Adds a double value to the current representation.
         */
        public ToStringBuilder<T> add(String name, double value) {
            nameEquals(name).append(value);
            return this;
        }

        /**
         * Adds a float value to the current representation.
         */
        public ToStringBuilder<T> add(String name, float value) {
            nameEquals(name).append(value);
            return this;
        }

        /**
         * Adds a potentially {@literal null} value to the current representation.
         */
        public ToStringBuilder<T> add(String name, @Nullable Object value, @Nullable Object nullValue) {
            if (value != null && value.getClass().isArray()) {
                // We don't know what kind of array, make it an object array and use deepToString on it
                String arrayToString = Arrays.deepToString(new Object[] { value });
                nameEquals(name).append(arrayToString.substring(1, arrayToString.length() - 1));
            } else if (value != null || nullValue != null) {
                // Append the value or the null placeholder
                nameEquals(name).append(value != null ? value : nullValue);
            } else {
                // If nothing else, just keep callers honest
                Objects.requireNonNull(name);
            }
            return this;
        }

        /**
         * Adds a value to the current representation unless it is {@literal null}.
         */
        public ToStringBuilder<T> add(String name, @Nullable Object value) {
            return add(name, value, null);
        }

        /**
         * Adds the result of invoking an accessor on object being represented.
         */
        public ToStringBuilder<T> add(String name, Function<T, Object> accessor) {
            return add(name, accessor.apply(self));
        }

        /**
         * Constructs the current representation string.
         * <p>
         * <em>IMPORTANT</em> This implementation clears the current state of the builder: two subsequent invocations
         * may return different values.
         */
        @Override
        public String toString() {
            StringBuilder result = value != null ? value : builder();
            value = null;
            return result.append('}').toString();
        }

        /**
         * Adds the supplied name and an equals sign to the current representation.
         */
        private StringBuilder nameEquals(String name) {
            Objects.requireNonNull(name);
            return builder().append(name).append('=');
        }

        /**
         * Returns the current representation, appending a delimiter if necessary.
         */
        private StringBuilder builder() {
            if (value != null) {
                return value.append(',').append(' ');
            } else {
                value = new StringBuilder(32).append(self.getClass().getSimpleName()).append('{');
                return value;
            }
        }
    }

    /**
     * Returns a helper for building a {@code toString} representation of the supplied object.
     */
    public static <T> ToStringBuilder<T> toString(T self) {
        return new ToStringBuilder<>(self);
    }

    /**
     * Returns a helper for building a {@code toString} representation of a child object, assuming the super has a
     * {@code toString} representation generated by {@code ToStringBuilder}.
     */
    public static <T> ToStringBuilder<T> toString(String superToString, T self) {
        int pos = superToString.indexOf('{');
        if (pos < 0 || !superToString.endsWith("}")) {
            // This isn't efficient, but it allows code to better cope with refactoring
            if (superToString.equals(self.getClass().getName() + '@' + Integer.toHexString(self.hashCode()))) {
                return new ToStringBuilder<>(self);
            } else {
                throw new IllegalArgumentException("invalid super toString: " + superToString);
            }
        } else {
            // Just append the part between the curly braces from the super
            ToStringBuilder<T> builder = new ToStringBuilder<>(self);
            builder.builder().append(superToString, pos + 1, superToString.length() - 1);
            return builder;
        }
    }

    /**
     * Returns a function that will either return the input cast to the requested type or {@literal null} if the input
     * is not of the correct type.
     * <p>
     * This is primarily useful in conjunction with {@code Optional.map}, e.g.
     * {@code someOptional.map(cast(String.class))}.
     */
    public static <T> Function<Object, T> cast(Class<T> type) {
        Objects.requireNonNull(type);
        return obj -> type.isInstance(obj) ? type.cast(obj) : null;
    }

    private ExtraObjects() {
        assert false;
    }
}
