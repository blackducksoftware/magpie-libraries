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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

public class AbstractChannelSubject<S extends AbstractChannelSubject<S, C>, C extends Channel> extends Subject<S, C> {

    protected AbstractChannelSubject(FailureMetadata metadata, C actual) {
        super(metadata, actual);
    }

    /**
     * Fails if the {@link Channel#close()} raises an exception when invoked on a closed channel.
     */
    public void hasIdempotentClose() throws IOException {
        checkArgument(actual().isOpen(), "channel was already closed");
        actual().close();

        if (actual().isOpen()) {
            failWithRawMessage("Closing the channel did not impact the return value of isOpen");
        } else {
            try {
                actual().close();
            } catch (Exception e) {
                throw new AssertionError("Closing a closed channel should not fail", e);
            }
        }
    }

    /**
     * Fails if the following methods do not produce a failure when invoked on a closed channel:
     * <ul>
     * <li>{@link ReadableByteChannel#read(java.nio.ByteBuffer)}</li>
     * <li>{@link WritableByteChannel#write(java.nio.ByteBuffer)}</li>
     * </ul>
     */
    public void failsWhenClosed() throws IOException {
        checkArgument(actual().isOpen(), "channel was already closed");
        actual().close();

        if (actual() instanceof ReadableByteChannel) {
            try {
                ((ReadableByteChannel) actual()).read(ByteBuffer.allocate(1));
                failWithRawMessage("Expected reading a closed channel to fail");
            } catch (ClosedChannelException e) {}
        }

        if (actual() instanceof WritableByteChannel) {
            try {
                ((WritableByteChannel) actual()).write(ByteBuffer.wrap(new byte[] { 1 }));
                failWithRawMessage("Expected writing a closed channel to fail");
            } catch (ClosedChannelException e) {}
        }
    }

}
