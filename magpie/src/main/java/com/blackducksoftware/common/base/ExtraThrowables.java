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

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Extra throwable helpers.
 *
 * @author jgustie
 */
public class ExtraThrowables {

    /**
     * Cleans up construction of an exception with a message. For example:
     *
     * <pre>
     * optional.orElseThrow(illegalArgument("Foo %s", "bar"))
     * </pre>
     *
     * Instead of:
     *
     * <pre>
     * optional.orElseThrow(() -&gt; new IllegalArgumentException("Foo " + "bar"))
     * </pre>
     */
    public static Supplier<IllegalArgumentException> illegalArgument(String errorMessageTemplate, Object... errorMessageArgs) {
        return () -> new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }

    /**
     * Cleans up construction of an exception with a message. For example:
     *
     * <pre>
     * optional.orElseThrow(illegalState("Foo %s", "bar"))
     * </pre>
     *
     * Instead of:
     *
     * <pre>
     * optional.orElseThrow(() -&gt; new IllegalStateException("Foo " + "bar"))
     * </pre>
     */
    public static Supplier<IllegalStateException> illegalState(String errorMessageTemplate, Object... errorMessageArgs) {
        return () -> new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
    }

    /**
     * Cleans up construction of an exception with a message. For example:
     *
     * <pre>
     * optional.orElseThrow(noSuchElement("Foo %s", "bar"))
     * </pre>
     *
     * Instead of:
     *
     * <pre>
     * optional.orElseThrow(() -&gt; new NoSuchElementException("Foo " + "bar"))
     * </pre>
     */
    public static Supplier<NoSuchElementException> noSuchElement(String errorMessageTemplate, Object... errorMessageArgs) {
        return () -> new NoSuchElementException(format(errorMessageTemplate, errorMessageArgs));
    }

    /**
     * Cleans up construction of an exception with a message. For example:
     *
     * <pre>
     * optional.orElseThrow(nullPointer("Foo %s", "bar"))
     * </pre>
     *
     * Instead of:
     *
     * <pre>
     * optional.orElseThrow(() -&gt; new NullPointerException("Foo " + "bar"))
     * </pre>
     */
    public static Supplier<NullPointerException> nullPointer(String errorMessageTemplate, Object... errorMessageArgs) {
        return () -> new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
    }

    /**
     * Helper to allow for call chaining on a {@code void} method.
     */
    public static <T extends Throwable> T addSuppressed(T throwable, Throwable suppressed) {
        throwable.addSuppressed(suppressed);
        return throwable;
    }

    /**
     * Helper to allow for typed call chaining.
     */
    public static <T extends Throwable> T initCause(T throwable, Throwable cause) {
        throwable.initCause(cause);
        return throwable;
    }

    /**
     * Helper to allow for typed call chaining.
     */
    public static <T extends Throwable> T fillInStackTrace(T throwable) {
        throwable.fillInStackTrace();
        return throwable;
    }

    /**
     * Formats an error message.
     */
    private static String format(String template, Object... args) {
        // TODO We should only support a subset of the full formatter
        return String.format(template, args);
    }

    private ExtraThrowables() {
        assert false;
    }
}
