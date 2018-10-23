/*
 * Copyright 2015 Black Duck Software, Inc.
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

import static com.google.common.io.BaseEncoding.base16;
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Truth.assertAbout;

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

/**
 * Subject for testing byte buffers.
 *
 * @author jgustie
 */
public final class ByteBufferSubject extends Subject<ByteBufferSubject, ByteBuffer> {

    public static Subject.Factory<ByteBufferSubject, ByteBuffer> byteBuffers() {
        return ByteBufferSubject::new;
    }

    public static ByteBufferSubject assertThat(@Nullable ByteBuffer target) {
        return assertAbout(byteBuffers()).that(target);
    }

    private ByteBufferSubject(FailureMetadata metadata, ByteBuffer actual) {
        // TODO Use slice so we don't mess with the actual buffer's state?
        super(metadata, actual);
    }

    @Override
    protected String actualCustomStringRepresentation() {
        if (actual() != null) {
            // TODO Include buffer contents
            return String.format("%s[pos=%d lim=%d cap=%d]",
                    actual().getClass().getSimpleName(), actual().position(), actual().limit(), actual().capacity());
        } else {
            return "null";
        }
    }

    public void hasNextBytes(byte... expected) {
        if (actual() == null) {
            failWithActual("expected buffer content", base16().encode(expected));
        } else if (actual().remaining() < expected.length) {
            failWithActual("expected buffer content", base16().encode(expected));
        } else {
            final int off = actual().position();
            for (int i = 0; i < expected.length; ++i) {
                if (actual().get(off + i) != expected[i]) {
                    failWithActual("expected buffer content", base16().encode(expected));
                }
            }
        }
    }

    public void hasRemaining(int remaining) {
        if (actual().remaining() != remaining) {
            failWithoutActual(
                    fact("expected remaining buffer capacity", remaining),
                    fact("but was", actual().remaining()));
        }
    }

}
