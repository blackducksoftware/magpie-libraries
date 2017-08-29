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
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    private BiFunction<String, String, String> combiner;

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

    @Test
    public void stream_null() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.stream(null);
    }

    @Test
    public void stream_empty() {
        // TODO Use Truth8 in 0.32
        assertThat(ExtraOptionals.stream(Optional.empty()).collect(toList())).isEmpty();
    }

    @Test
    public void stream_present() {
        // TODO Use Truth8 in 0.32
        assertThat(ExtraOptionals.stream(Optional.of("test")).collect(toList())).containsExactly("test");
    }

    // TODO mapToObj

    @Test
    public void ofType_null() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.ofType(null);
    }

    @Test
    public void ofType() {
        assertThat(ExtraOptionals.ofType(String.class).apply("test")).hasValue("test");
        assertThat(ExtraOptionals.ofType(String.class).apply(Integer.valueOf(0))).isEmpty();
        assertThat(ExtraOptionals.ofType(String.class).apply(null)).isEmpty();
    }

    @Test
    public void and_nullA() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.and(null, Optional.empty(), combiner);
    }

    @Test
    public void and_nullB() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.and(Optional.empty(), null, combiner);
    }

    @Test
    public void and_nullCombiner() {
        thrown.expect(NullPointerException.class);

        ExtraOptionals.and(Optional.empty(), Optional.empty(), null);
    }

    @Test
    public void and_emptyA() {
        assertThat(ExtraOptionals.and(Optional.empty(), Optional.of("test"), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void and_emptyB() {
        assertThat(ExtraOptionals.and(Optional.of("test"), Optional.empty(), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void and_emptyA_emptyB() {
        assertThat(ExtraOptionals.and(Optional.empty(), Optional.empty(), combiner)).isEmpty();
        verify(combiner, never()).apply(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void and() {
        when(combiner.apply("a", "b")).thenReturn("c");
        // Using the method reference to the mock ensures default implementations work
        assertThat(ExtraOptionals.and(Optional.of("a"), Optional.of("b"), combiner::apply)).hasValue("c");
    }

}