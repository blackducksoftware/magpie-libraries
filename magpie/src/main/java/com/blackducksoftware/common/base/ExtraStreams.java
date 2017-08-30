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

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Extra stream helpers.
 *
 * @author jgustie
 */
public final class ExtraStreams {

    /**
     * A function for filtering a stream by type. For example:
     *
     * <pre>
     * List&lt;String&gt; strs = objs.stream().flatMap(ofType(String.class)).collect(toList());
     * </pre>
     *
     * Is another way of doing:
     *
     * <pre>
     * List&lt;String&gt; strs = objs.stream().filter(String.class::isInstance).map(String.class::cast).collect(toList());
     * </pre>
     *
     * Which is another way of doing:
     *
     * <pre>
     * List&lt;String&gt; strs = new ArrayList&lt;&gt;();
     * for (Object obj : objs) {
     *     if (obj instanceof String) {
     *         strs.add((String) obj);
     *     }
     * }
     * </pre>
     */
    public static <T> Function<Object, Stream<T>> ofType(Class<T> type) {
        Objects.requireNonNull(type);
        return obj -> type.isInstance(obj) ? Stream.of(type.cast(obj)) : Stream.empty();
    }

    private ExtraStreams() {
        assert false;
    }
}
