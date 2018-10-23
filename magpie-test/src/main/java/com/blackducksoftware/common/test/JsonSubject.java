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
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Iterables;
import com.google.common.truth.DefaultSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

/**
 * Subject for testing JSON.
 *
 * @author jgustie
 */
public class JsonSubject extends Subject<JsonSubject, JsonNode> {

    /**
     * Jackson module used internally to initialize the object mapper.
     */
    private static class JsonSubjectDefaultModule extends Module {
        private static final JsonSubjectDefaultModule INSTANCE = new JsonSubjectDefaultModule();

        private boolean indentOutput;

        @Override
        public String getModuleName() {
            return "JsonSubject.DEFAULT";
        }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public void setupModule(SetupContext context) {
            // Not supposed to do this, but this is kind of an end-around anyway
            ObjectMapper mapper = context.getOwner();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, indentOutput);
        }
    }

    public static class JsonSubjectFactory implements Subject.Factory<JsonSubject, JsonNode> {

        private final ObjectMapper objectMapper;

        private JsonSubjectFactory() {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(JsonSubjectDefaultModule.INSTANCE);
        }

        // TODO Register other modules via method calls?

        public JsonSubjectFactory configure(DeserializationFeature feature, boolean state) {
            objectMapper.configure(feature, state);
            return this;
        }

        public JsonSubjectFactory configure(JsonParser.Feature feature, boolean state) {
            objectMapper.configure(feature, state);
            return this;
        }

        @Override
        public JsonSubject createSubject(FailureMetadata metadata, JsonNode actual) {
            return new JsonSubject(metadata, actual, this);
        }

        public JsonSubject assertThatJson(String content) {
            return Truth.assertAbout(this).that(readTree(content));
        }

        private JsonNode readTree(String content) {
            // TODO Should this be cached?
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

    public static void prettyPrintSubjects() {
        JsonSubjectDefaultModule.INSTANCE.indentOutput = true;
    }

    /**
     * Factory that created this subject. Used to preserve parser settings and perform additional parsing if necessary.
     */
    private final JsonSubjectFactory factory;

    private JsonSubject(FailureMetadata metadata, JsonNode actual, JsonSubjectFactory factory) {
        super(metadata, actual);
        this.factory = Objects.requireNonNull(factory);
    }

    /**
     * When formatting the subject, use the factory object mapper.
     */
    @Override
    protected String actualCustomStringRepresentation() {
        try {
            return factory.objectMapper.writeValueAsString(actual());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
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
            failWithActual("expected match at pointer", pointer);
        }
        return check().about(factory).that(next);
    }

    // TODO Should we just pick one of these two forms ("isX", "xAt")?

    public IterableSubject isArray() {
        if (actual() == null || !actual().isArray()) {
            failWithActual(simpleFact("expected array"));
        }
        return check().that((Iterable<?>) JsonUtil.unwrap(actual()));
    }

    public IterableSubject arrayAt(String pointer) {
        return at(pointer).isArray();
    }

    public StringSubject isTextual() {
        if (actual() == null || !actual().isTextual()) {
            failWithActual(simpleFact("expected textual"));
        }
        return check().that(actual().asText());
    }

    public StringSubject textAt(String pointer) {
        return at(pointer).isTextual();
    }

    public IntegerSubject isInteger() {
        if (actual() == null || !actual().isInt()) {
            failWithActual(simpleFact("expected integer"));
        }
        return check().that(actual().intValue());
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
            failWithActual(simpleFact("expected object"));
        } else if (!actual().has(name)) {
            failWithActual("expected to contain name", name);
        }
    }

    /**
     * Fails if the subject contains a name/value pair with the specified name.
     */
    public void doesNotContainName(String name) {
        checkNotNull(name);
        if (actual() == null || !actual().isObject()) {
            failWithActual(simpleFact("expected object"));
        } else if (actual().has(name)) {
            failWithActual("expected not to contain name", name);
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
            failWithActual(simpleFact("expected object"));
        } else if (actual().has(name)) {
            at(name).isNotEqualTo(value);
        }
    }

    /**
     * Fails if the subject is a container with one ore more values or name/value pairs.
     */
    public void isEmpty() {
        if (actual() == null || !actual().isContainerNode()) {
            failWithActual(simpleFact("expected container"));
        } else if (actual().size() != 0) {
            failWithActual(simpleFact("expected empty"));
        }
    }

    /**
     * Fails if the subject is an empty container.
     */
    public void isNotEmpty() {
        if (actual() == null || !actual().isContainerNode()) {
            failWithActual(simpleFact("expected container"));
        } else if (actual().size() == 0) {
            failWithActual(simpleFact("expected not empty"));
        }
    }

    /**
     * Fails if the subject is not a container with the specified number of values or name/value pairs.
     */
    public void hasLength(int length) {
        // TODO Support string length?
        checkArgument(length >= 0, "length (%s) must be >= 0", length);
        if (actual() == null || !actual().isContainerNode()) {
            failWithActual(simpleFact("expected empty"));
        } else if (actual().size() != length) {
            failWithActual("expected length", length);
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
            failWithActual(simpleFact("expected null"));
        }
    }

    @Override
    public void isNotNull() {
        if (actual() == null || actual().isNull()) {
            failWithActual(simpleFact("expected non null"));
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
        if (!clazz.isInstance(actual()) && !JsonUtil.isInstanceOfType(actual(), clazz)) {
            if (actual() != null) {
                failWithoutActual(
                        fact("expected instance of", clazz.getName()),
                        fact(actual().isValueNode() ? "but has node type of" : "but was instance of", JsonUtil.typeName(actual())),
                        fact("with value", actualAsString()));
            } else {
                failWithActual("expected instance of", clazz.getName());
            }
        }
    }

    @Override
    public void isNotInstanceOf(Class<?> clazz) {
        checkNotNull(clazz);
        if (actual() != null && (clazz.isInstance(actual()) || JsonUtil.isInstanceOfType(actual(), clazz))) {
            failWithActual("expected not to be an instance of", clazz.getName());
        }
    }

    @Override
    public void isIn(Iterable<?> iterable) {
        if (!Iterables.contains(iterable, actual()) && !Iterables.contains(iterable, JsonUtil.unwrap(actual()))) {
            failWithActual("expected any of", iterable);
        }
    }

    @Override
    public void isNotIn(Iterable<?> iterable) {
        int index = Iterables.indexOf(iterable, e -> Objects.equals(e, actual()) || Objects.equals(e, JsonUtil.unwrap(actual())));
        if (index != -1) {
            failWithActual("expected not to be any of", iterable);
        }
    }

    /**
     * Returns a subject conditionally around the unwrapped actual value. Basically, if we are comparing to another JSON
     * node, we don't want to unwrap the actual value.
     */
    private Subject<DefaultSubject, Object> actualFor(Object other) {
        return check().that(other instanceof JsonNode ? actual() : JsonUtil.unwrap(actual()));
    }

}
