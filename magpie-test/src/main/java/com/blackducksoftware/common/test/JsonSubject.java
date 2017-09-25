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
package com.blackducksoftware.common.test;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.truth.DefaultSubject;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Platform;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.TestVerb;
import com.google.common.truth.Truth;

/**
 * Subject for testing JSON.
 *
 * @author jgustie
 */
public class JsonSubject extends Subject<JsonSubject, JsonNode> {

    public static class JsonSubjectFactory extends SubjectFactory<JsonSubject, JsonNode> {

        private final ObjectMapper objectMapper;

        private JsonSubjectFactory() {
            objectMapper = new ObjectMapper();

        }

        // TODO Register modules?

        public JsonSubjectFactory configure(DeserializationFeature feature, boolean state) {
            objectMapper.configure(feature, state);
            return this;
        }

        public JsonSubjectFactory configure(JsonParser.Feature feature, boolean state) {
            objectMapper.configure(feature, state);
            return this;
        }

        @Override
        public JsonSubject getSubject(FailureStrategy fs, JsonNode that) {
            return new JsonSubject(fs, this, that);
        }

        public JsonSubject assertThatJson(String content) {
            return Truth.assertAbout(this).that(readTree(content));
        }

        private JsonNode readTree(String content) {
            try {
                return objectMapper.readTree(content);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static JsonSubjectFactory json() {
        return new JsonSubjectFactory();
    }

    public static JsonSubject assertThat(JsonNode target) {
        return Truth.assertAbout(json()).that(target);
    }

    public static JsonSubject assertThatJson(String content) {
        return json().assertThatJson(content);
    }

    /**
     * Test verb used to perform further assertions.
     */
    private final TestVerb assert_;

    /**
     * Factory that created this subject. Used to preserve parser settings and perform additional parsing if necessary.
     */
    private final JsonSubjectFactory factory;

    private JsonSubject(FailureStrategy failureStrategy, JsonSubjectFactory factory, JsonNode actual) {
        super(failureStrategy, actual);
        this.factory = Objects.requireNonNull(factory);
        this.assert_ = new TestVerb(failureStrategy);
    }

    /**
     * Fails if the supplied JSON pointer does not match, otherwise returns a new subject for additional verification.
     * Generates a JSON Pointer string using {@code toString} representations of the supplied sequence. For example,
     * {@code at("foo", "bar")} is the same as {@code at("/foo/bar")}.
     */
    public JsonSubject at(Object... tokens) {
        // Assume a single element starting with "/" is just a valid pointer, otherwise build it
        checkArgument(tokens != null && tokens.length > 0);
        String pointer;
        if (tokens.length == 1 && tokens[0].toString().startsWith("/")) {
            pointer = tokens[0].toString();
        } else {
            pointer = Stream.of(tokens)
                    .map(Object::toString)
                    .map(s -> s.replace("~", "~0"))
                    .map(s -> s.replace("/", "~1"))
                    .collect(Collectors.joining("/", "/", ""));
        }

        // Resolve the JSON node using the pointer
        JsonNode next = actual().at(pointer);
        if (next.isMissingNode()) {
            fail("matches anything at pointer", pointer);
        }
        return factory.getSubject(failureStrategy, next);
    }

    // TODO Should we just pick one of these two forms ("isX", "xAt")?

    public IterableSubject isArray() {
        if (actual() == null || !actual().isArray()) {
            fail("is an array");
        }
        return assert_.that((Iterable<?>) JsonUtil.unwrap(actual()));
    }

    public IterableSubject arrayAt(String pointer) {
        return at(pointer).isArray();
    }

    public StringSubject isTextual() {
        if (actual() == null || !actual().isTextual()) {
            fail("is textual");
        }
        return assert_.that(actual().asText());
    }

    public StringSubject textAt(String pointer) {
        return at(pointer).isTextual();
    }

    public IntegerSubject isInteger() {
        if (actual() == null || !actual().isInt()) {
            fail("is integer");
        }
        return assert_.that(actual().intValue());
    }

    public IntegerSubject integerAt(String pointer) {
        return at(pointer).isInteger();
    }

    /**
     * Fails if the subject does not have name/value pair with the specified name.
     */
    public void containsName(String name) {
        checkNotNull(name);
        if (actual() == null || !actual().isObject()) {
            fail("is an object");
        } else if (!actual().has(name)) {
            fail("contains name", name);
        }
    }

    /**
     * Fails if the subject contains a name/value pair with the specified name.
     */
    public void doesNotContainName(String name) {
        checkNotNull(name);
        if (actual() == null || !actual().isObject()) {
            fail("is an object");
        } else if (actual().has(name)) {
            fail("does not contain name", name);
        }
    }

    /**
     * Fails if the subject does not have the specified name/value pair.
     */
    public void containsPair(String name, @Nullable Object value) {
        // Just gives a more friendly message
        containsName(name);
        at(name).isEqualTo(value);
    }

    /**
     * Fails if the subject has the specified name/value pair.
     */
    public void doesNotContainPair(String name, @Nullable Object value) {
        checkNotNull(name);
        if (actual() == null || !actual().isObject()) {
            fail("is an object");
        } else if (actual().has(name)) {
            at(name).isNotEqualTo(value);
        }
    }

    /**
     * Fails if the subject is a container with one ore more values or name/value pairs.
     */
    public void isEmpty() {
        if (actual() == null || !actual().isContainerNode()) {
            fail("is a container");
        } else if (actual().size() != 0) {
            fail("is empty");
        }
    }

    /**
     * Fails if the subject is an empty container.
     */
    public void isNotEmpty() {
        if (actual() == null || !actual().isContainerNode()) {
            fail("is a container");
        } else if (actual().size() == 0) {
            fail("is not empty");
        }
    }

    /**
     * Fails if the subject is not a container with the specified number of values or name/value pairs.
     */
    public void hasLength(int length) {
        // TODO Support string length?
        checkArgument(length >= 0, "length (%s) must be >= 0", length);
        if (actual() == null || !actual().isContainerNode()) {
            fail("is a container");
        } else if (actual().size() != length) {
            fail("has length", length);
        }
    }

    /**
     * Fails if the subject is not equal to the given unparsed JSON. Comparison is done on the parsed JSON tree
     * structure and is not effected by formatting differences (like whitespace).
     */
    public void isEqualToJson(String content) {
        isEqualTo(factory.readTree(content));
    }

    @Override
    public void isNull() {
        if (actual() != null && !actual().isNull()) {
            fail("is null");
        }
    }

    @Override
    public void isNotNull() {
        if (actual() == null || actual().isNull()) {
            fail("is not null");
        }
    }

    @Override
    public void isEqualTo(Object other) {
        actualFor(other).isEqualTo(other);
    }

    @Override
    public void isNotEqualTo(Object other) {
        actualFor(other).isNotEqualTo(other);
    }

    @Override
    public void isInstanceOf(Class<?> clazz) {
        checkNotNull(clazz);
        if (!Platform.isInstanceOfType(actual(), clazz) && !JsonUtil.isInstanceOfType(actual(), clazz)) {
            if (actual() != null) {
                failWithBadResults("is an instance of", clazz.getName(),
                        actual().isValueNode() ? "has a node type of" : "is an instance of", JsonUtil.typeName(actual()));
            } else {
                fail("is an instance of", clazz.getName());
            }
        }
    }

    @Override
    public void isNotInstanceOf(Class<?> clazz) {
        checkNotNull(clazz);
        if (actual() != null && (Platform.isInstanceOfType(actual(), clazz) || JsonUtil.isInstanceOfType(actual(), clazz))) {
            failWithRawMessage("%s expected not to be an instance of %s, but was.", actualAsString(), clazz.getName());
        }
    }

    @Override
    public void isIn(Iterable<?> iterable) {
        if (!Iterables.contains(iterable, actual()) && !Iterables.contains(iterable, JsonUtil.unwrap(actual()))) {
            fail("is equal to any element in", iterable);
        }
    }

    @Override
    public void isNotIn(Iterable<?> iterable) {
        int index = Iterables.indexOf(iterable, e -> Objects.equals(e, actual()) || Objects.equals(e, JsonUtil.unwrap(actual())));
        if (index != -1) {
            failWithRawMessage("Not true that %s is not in %s. It was found at index %s", actualAsString(), iterable, index);
        }
    }

    @Override
    public void isAnyOf(Object first, Object second, Object... rest) {
        List<Object> list = Lists.asList(first, second, rest);
        if (!list.contains(actual()) && !list.contains(JsonUtil.unwrap(actual()))) {
            fail("is equal to any of", list);
        }
    }

    @Override
    public void isNoneOf(Object first, Object second, Object... rest) {
        isNotIn(Lists.asList(first, second, rest));
    }

    /**
     * Returns a subject conditionally around the unwrapped actual value. Basically, if we are comparing to another JSON
     * node, w don't want to unwrap the actual value.
     */
    private Subject<DefaultSubject, Object> actualFor(Object other) {
        return assert_.that(other instanceof JsonNode ? actual() : JsonUtil.unwrap(actual()));
    }

}
