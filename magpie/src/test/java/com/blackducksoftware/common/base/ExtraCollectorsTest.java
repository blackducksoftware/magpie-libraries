/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.common.base;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Tests for {@code ExtraCollectors}.
 *
 * @author jgustie
 */
public class ExtraCollectorsTest {

    @Test
    public void getOnlyEmpty() {
        assertThat(Stream.empty().collect(ExtraCollectors.getOnly())).isEmpty();
    }

    @Test
    public void getOnlySingleton() {
        assertThat(Stream.of("x").collect(ExtraCollectors.getOnly())).hasValue("x");
    }

    @Test(expected = IllegalStateException.class)
    public void getOnlyMultiple() {
        Stream.of("x", "y").collect(ExtraCollectors.getOnly());
    }

    @Test
    public void getOnlySplitEmpty() {
        assertThat(splitStream(null, null).collect(ExtraCollectors.getOnly())).isEmpty();
    }

    @Test
    public void getOnlySplitSingleFirst() {
        assertThat(splitStream("x", null).collect(ExtraCollectors.getOnly())).hasValue("x");
    }

    @Test
    public void getOnlySplitSingleSecond() {
        assertThat(splitStream(null, "x").collect(ExtraCollectors.getOnly())).hasValue("x");
    }

    @Test(expected = IllegalStateException.class)
    public void getOnlySplitMultiple() {
        splitStream("x", "y").collect(ExtraCollectors.getOnly());
    }

    @Test
    public void toImmutableListEmpty() {
        List<String> list = Stream.<String> empty().collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).isEmpty();
    }

    @Test
    public void toImmutableListSingleton() {
        List<String> list = Stream.of("x").collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).containsExactly("x").inOrder();
    }

    @Test
    public void toImmutableListMultiple() {
        List<String> list = Stream.of("x", "y").collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).containsExactly("x", "y").inOrder();
    }

    @Test
    public void toImmutableListSplitEmpty() {
        List<String> list = splitStream(null, null).collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).isEmpty();
    }

    @Test
    public void toImmutableListSplitSingleFirst() {
        List<String> list = splitStream("x", null).collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).containsExactly("x");
    }

    @Test
    public void toImmutableListSplitSingleSecond() {
        List<String> list = splitStream(null, "x").collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).containsExactly("x");
    }

    @Test
    public void toImmutableListSplitMultiple() {
        List<String> list = splitStream("x", "y").collect(ExtraCollectors.toImmutableList());
        assertThat(list).isInstanceOf(ImmutableList.class);
        assertThat(list).containsExactly("x", "y");
    }

    @Test
    public void toImmutableSetEmpty() {
        Set<String> set = Stream.<String> empty().collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).isEmpty();
    }

    @Test
    public void toImmutableSetSingleton() {
        Set<String> set = Stream.of("x").collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).containsExactly("x");
    }

    @Test
    public void toImmutableSetMultiple() {
        Set<String> set = Stream.of("x", "y").collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).containsExactly("x", "y");
    }

    @Test
    public void toImmutableSetMultipleUnique() {
        Set<String> set = Stream.of("x", "y", "y", "x").collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).containsExactly("x", "y");
    }

    @Test
    public void toImmutableSetSplitEmpty() {
        Set<String> set = splitStream(null, null).collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).isEmpty();
    }

    @Test
    public void toImmutableSetSplitSingleFirst() {
        Set<String> set = splitStream("x", null).collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).containsExactly("x");
    }

    @Test
    public void toImmutableSetSplitSingleSecond() {
        Set<String> set = splitStream(null, "x").collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).containsExactly("x");
    }

    @Test
    public void toImmutableSetSplitMultiple() {
        Set<String> set = splitStream("x", "y").collect(ExtraCollectors.toImmutableSet());
        assertThat(set).isInstanceOf(ImmutableSet.class);
        assertThat(set).containsExactly("x", "y");
    }

    /**
     * Returns a stream that has zero, one or two elements. The spliterator used to back the stream will always include
     * the first element, the second element will part of the split when
     */
    public static Stream<String> splitStream(@Nullable String element1, @Nullable String element2) {
        final int characteristics = Spliterator.CONCURRENT;
        final AtomicReference<Spliterator<String>> elements1 = new AtomicReference<>(Spliterators.spliteratorUnknownSize(
                element1 != null ? Collections.singleton(element1).iterator() : Collections.emptyIterator(), characteristics));
        final AtomicReference<Spliterator<String>> elements2 = new AtomicReference<>(Spliterators.spliteratorUnknownSize(
                element2 != null ? Collections.singleton(element2).iterator() : Collections.emptyIterator(), characteristics));
        return StreamSupport.stream(new Spliterator<String>() {
            @Override
            public boolean tryAdvance(Consumer<? super String> action) {
                Spliterator<String> e1 = elements1.get();
                if (e1 != null) {
                    if (e1.tryAdvance(action)) {
                        return true;
                    } else {
                        // Handle the case where trySplit wasn't invoked
                        elements1.compareAndSet(e1, elements2.getAndSet(null));
                        return tryAdvance(action);
                    }
                } else {
                    return false;
                }
            }

            @Override
            public Spliterator<String> trySplit() {
                return elements2.getAndSet(null);
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return characteristics;
            }
        }, true);
    }

}
