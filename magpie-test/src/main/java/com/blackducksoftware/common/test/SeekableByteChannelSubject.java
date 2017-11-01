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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

public final class SeekableByteChannelSubject extends AbstractChannelSubject<SeekableByteChannelSubject, SeekableByteChannel> {

    private static final Subject.Factory<SeekableByteChannelSubject, SeekableByteChannel> FACTORY = new Subject.Factory<SeekableByteChannelSubject, SeekableByteChannel>() {
        @Override
        public SeekableByteChannelSubject createSubject(FailureMetadata metadata, SeekableByteChannel actual) {
            return new SeekableByteChannelSubject(metadata, actual);
        }
    };

    public static Subject.Factory<SeekableByteChannelSubject, SeekableByteChannel> seekableByteChannels() {
        return FACTORY;
    }

    public static SeekableByteChannelSubject assertThat(SeekableByteChannel target) {
        return Truth.assertAbout(seekableByteChannels()).that(target);
    }

    private SeekableByteChannelSubject(FailureMetadata metadata, SeekableByteChannel actual) {
        super(metadata, actual);
    }

    /**
     * Fails if the channel does not have the given size.
     */
    public void hasSize(long expectedSize) throws IOException {
        checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
        long actualSize = actual().size();
        if (actualSize != expectedSize) {
            failWithBadResults("has a size of", expectedSize, "is", actualSize);
        }
    }

    /**
     * Fails if the channel is not positioned at the given offset.
     */
    public void isPositionedAt(long expectedPosition) throws IOException {
        checkArgument(expectedPosition >= 0, "expectedPosition(%s) must be >= 0", expectedPosition);
        long actualPosition = actual().position();
        if (actualPosition != expectedPosition) {
            failWithBadResults("is positioned at offset", expectedPosition, "is", actualPosition);
        }
    }

    /**
     * Fails if the following methods do not produce a failure when invoked on a closed channel:
     * <ul>
     * <li>{@link SeekableByteChannel#read(java.nio.ByteBuffer)}</li>
     * <li>{@link SeekableByteChannel#write(java.nio.ByteBuffer)}</li>
     * <li>{@link SeekableByteChannel#position()}</li>
     * <li>{@link SeekableByteChannel#position(long)}</li>
     * <li>{@link SeekableByteChannel#size()}</li>
     * <li>{@link SeekableByteChannel#truncate(long)}</li>
     * </ul>
     */
    @Override
    public void failsWhenClosed() throws IOException {
        super.failsWhenClosed();

        try {
            actual().position();
            failWithRawMessage("Expected retrieving the position of a closed channel to fail");
        } catch (ClosedChannelException e) {}

        try {
            actual().position(0L);
            failWithRawMessage("Expected positioning of a closed channel to fail");
        } catch (ClosedChannelException e) {}

        try {
            actual().size();
            failWithRawMessage("Expected retrieving the size of a closed channel to fail");
        } catch (ClosedChannelException e) {}

        try {
            actual().truncate(0L);
            failWithRawMessage("Expected retrieving the position of a closed channel to fail");
        } catch (ClosedChannelException | NonWritableChannelException e) {}
    }

}
