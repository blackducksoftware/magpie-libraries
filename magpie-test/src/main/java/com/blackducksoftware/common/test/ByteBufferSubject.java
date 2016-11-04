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

import java.nio.ByteBuffer;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.Truth;

/**
 * Subject for testing byte buffers.
 *
 * @author jgustie
 */
public class ByteBufferSubject extends Subject<ByteBufferSubject, ByteBuffer> {

    private static final SubjectFactory<ByteBufferSubject, ByteBuffer> FACTORY = new SubjectFactory<ByteBufferSubject, ByteBuffer>() {
        @Override
        public ByteBufferSubject getSubject(FailureStrategy fs, ByteBuffer that) {
            return new ByteBufferSubject(fs, that);
        }
    };

    public static ByteBufferSubject assertThatByteBuffer(ByteBuffer target) {
        return Truth.assertAbout(FACTORY).that(target);
    }

    public static SubjectFactory<ByteBufferSubject, ByteBuffer> byteBuffers() {
        return FACTORY;
    }

    private ByteBufferSubject(FailureStrategy failureStrategy, ByteBuffer subject) {
        super(failureStrategy, subject);
    }

    public void hasNextBytes(byte... expected) {
        if (getSubject() == null) {
            fail("hasNextBytes", expected);
        } else if (getSubject().remaining() < expected.length) {
            fail("hasNextBytes", expected);
        } else {
            final int off = getSubject().position();
            for (int i = 0; i < expected.length; ++i) {
                if (getSubject().get(off + i) != expected[i]) {
                    fail("hasNextBytes", expected);
                }
            }
        }
    }

}
