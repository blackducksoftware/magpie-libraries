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
package com.blackducksoftware.common.base;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@code ExtraOptionals}.
 *
 * @author jgustie
 */
public class ExtraOptionalsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Consumer<String> action;

    @Mock
    private Runnable emptyAction;

    @Mock
    private Supplier<Optional<String>> supplier;

    @Mock
    private Function<String, String> function;

    @Mock
    private BinaryOperator<String> combiner;

    @Mock
    private BiConsumer<String, String> consumer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void ifPresentOrElse_nullOptional() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.ifPresentOrElse(null, action, emptyAction);
    }

    @Test
    public void ifPresentOrElse_nullAction() {
        // Compatibility with Java 9
        ExtraOptionals.ifPresentOrElse(Optional.empty(), null, emptyAction);

        thrown.expect(NullPointerException.class);

        ExtraOptionals.ifPresentOrElse(Optional.of("test"), null, emptyAction);
    }

    @Test
    public void ifPresentOrElse_nullEmptyAction() {
        // Compatibility with Java 9
        ExtraOptionals.ifPresentOrElse(Optional.of("test"), action, null);

        thrown.expect(NullPointerException.class);

        ExtraOptionals.ifPresentOrElse(Optional.empty(), action, null);
    }

    @Test
    public void ifPresentOrElse_empty() {
        ExtraOptionals.ifPresentOrElse(Optional.empty(), action, emptyAction);

        verify(action, never()).accept(Mockito.any());
        verify(emptyAction).run();
    }

    @Test
    public void ifPresentOrElse_present() {
        ExtraOptionals.ifPresentOrElse(Optional.of("test"), action, emptyAction);

        verify(action).accept("test");
        verify(emptyAction, never()).run();
    }

    @Test
    public void or_nullSelf() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.or(null, supplier);
    }

    @Test
    public void or_emptySelf_nullSupplier() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.or(Optional.empty(), null);
    }

    @Test
    public void or_presentSelf_nullSupplier() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.or(Optional.of("test"), null);
    }

    @Test
    public void or_supplierReturnsNull() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.or(Optional.empty(), () -> null);
    }

    @Test
    public void or_emptySelf() {
        when(supplier.get()).thenReturn(Optional.of("test"));

        assertThat(ExtraOptionals.or(Optional.empty(), supplier)).hasValue("test");
    }

    @Test
    public void or_presentSelf() {
        ExtraOptionals.or(Optional.of("test"), supplier);

        verify(supplier, never()).get();
    }

    // TODO mapToObj

    @Test
    public void isOptionalEquals_null() {
        assertThat(ExtraOptionals.isOptionalEqual(null).test(Optional.ofNullable(null))).isTrue();
        assertThat(ExtraOptionals.isOptionalEqual(null).test(Optional.ofNullable("test"))).isFalse();
    }

    @Test
    public void isOptionalEquals_present() {
        assertThat(ExtraOptionals.isOptionalEqual("test").test(Optional.ofNullable(null))).isFalse();
        assertThat(ExtraOptionals.isOptionalEqual("test").test(Optional.ofNullable("test"))).isTrue();
        assertThat(ExtraOptionals.isOptionalEqual("test").test(Optional.ofNullable("foobar"))).isFalse();
    }

    @Test
    public void isEmpty() {
        assertThat(ExtraOptionals.isEmpty().test(Optional.empty())).isTrue();
        assertThat(ExtraOptionals.isEmpty().test(Optional.of("test"))).isFalse();
    }

    @Test
    public void ifPresent_nullA() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.ifPresent(null, Optional.empty(), consumer);
    }

    @Test
    public void ifPresent_nullB() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.ifPresent(Optional.empty(), null, consumer);
    }

    @Test
    public void ifPresent_nullConsumer() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.ifPresent(Optional.empty(), Optional.empty(), null);
    }

    @Test
    public void ifPresent_emptyA() {
        ExtraOptionals.ifPresent(Optional.empty(), Optional.of("test"), consumer);
        verify(consumer, never()).accept(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void ifPresent_emptyB() {
        ExtraOptionals.ifPresent(Optional.of("test"), Optional.empty(), consumer);
        verify(consumer, never()).accept(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void ifPresent_emptyA_emptyB() {
        ExtraOptionals.ifPresent(Optional.empty(), Optional.empty(), consumer);
        verify(consumer, never()).accept(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void ifPresent() {
        ExtraOptionals.ifPresent(Optional.of("a"), Optional.of("b"), consumer);
        verify(consumer, only()).accept("a", "b");
    }

    @Test
    public void map_nullA() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.map(null, Optional.empty(), combiner);
    }

    @Test
    public void map_nullB() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.map(Optional.empty(), null, combiner);
    }

    @Test
    public void map_nullMapper() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.map(Optional.empty(), Optional.empty(), null);
    }

    @Test
    public void map_emptyA() {
        assertThat(ExtraOptionals.map(Optional.empty(), Optional.of("test"), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void map_emptyB() {
        assertThat(ExtraOptionals.map(Optional.of("test"), Optional.empty(), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void map_emptyA_emptyB() {
        assertThat(ExtraOptionals.map(Optional.empty(), Optional.empty(), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void map() {
        when(combiner.apply("a", "b")).thenReturn("c");
        assertThat(ExtraOptionals.map(Optional.of("a"), Optional.of("b"), combiner)).hasValue("c");
    }

    @Test
    public void merge_nullA() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.merge(null, Optional.empty(), combiner);
    }

    @Test
    public void merge_nullB() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.merge(Optional.empty(), null, combiner);
    }

    @Test
    public void merge_nullCombiner() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.merge(Optional.empty(), Optional.empty(), null);
    }

    @Test
    public void merge_emptyA() {
        assertThat(ExtraOptionals.merge(Optional.empty(), Optional.of("test"), combiner)).hasValue("test");
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void merge_emptyB() {
        assertThat(ExtraOptionals.merge(Optional.of("test"), Optional.empty(), combiner)).hasValue("test");
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void merge_emptyA_emptyB() {
        assertThat(ExtraOptionals.merge(Optional.empty(), Optional.empty(), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void merge() {
        when(combiner.apply("a", "b")).thenReturn("c");
        // Using the method reference to the mock ensures default implementations work
        assertThat(ExtraOptionals.merge(Optional.of("a"), Optional.of("b"), combiner::apply)).hasValue("c");
    }

    @Test
    public void flatMapThrowable_empty() {
        assertThat(ExtraOptionals.flatMapThrowable(Optional.empty(), function)).isEmpty();
        verify(function, never()).apply(Mockito.anyString());
    }

    @Test
    public void flatMapThrowable_identity() {
        assertThat(ExtraOptionals.flatMapThrowable(Optional.of("test"), Function.identity())).hasValue("test");
    }

    @Test
    public void flatMapThrowable_functionThrows() {
        assertThat(ExtraOptionals.flatMapThrowable(Optional.of("test"), x -> {
            throw new RuntimeException();
        })).isEmpty();
    }

    @Test
    public void flatMapMany_null() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.flatMapMany(null, Stream::of);
    }

    @Test
    public void flatMapMany_nullFunction() {
        thrown.expect(NullPointerException.class);
        ExtraOptionals.flatMapMany(Optional.empty(), null);
    }

    @Test
    public void flatMapMany_empty() {
        assertThat(ExtraOptionals.flatMapMany(Optional.empty(), Stream::of)).isEmpty();
    }

    @Test
    public void flatMapMany_present() {
        assertThat(ExtraOptionals.flatMapMany(Optional.of("test"), Stream::of)).containsExactly("test");
    }

    @Test
    public void flatMapMany_multiple() {
        assertThat(ExtraOptionals.flatMapMany(Optional.of("test"),
                t -> t.chars().mapToObj(cp -> String.valueOf(Character.toChars(cp)))))
                        .containsExactly("t", "e", "s", "t").inOrder();
    }

}
