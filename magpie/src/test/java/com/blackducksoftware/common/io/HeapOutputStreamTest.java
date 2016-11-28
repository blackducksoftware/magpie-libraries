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
package com.blackducksoftware.common.io;

import static com.blackducksoftware.common.test.SeekableByteChannelSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import org.junit.Test;

import com.google.common.io.ByteSource;

/**
 * Tests for {@link HeapOutputStream}.
 *
 * @author jgustie
 */
public class HeapOutputStreamTest {

    @Test
    public void capacity() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream(16)) {
            assertThat(out.hasCapacity(15)).isTrue();
            assertThat(out.hasCapacity(16)).isTrue();
            assertThat(out.hasCapacity(17)).isFalse();
        }
    }

    @Test(expected = NonWritableChannelException.class)
    public void readOnlyChannel() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            try (SeekableByteChannel channel = out.getChannel()) {
                channel.write(ByteBuffer.wrap(new byte[] { 0x00 }));
            }
        }
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void readOnlyBuffer() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            out.toByteBuffer().put((byte) 0x00);
        }
    }

    @Test
    public void channelSnapshot() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            SeekableByteChannel channel = out.getChannel();

            out.write(1);
            assertThat(out.getChannel()).hasSize(1L);

            // Because we wrote to `out` after creating `channel` the contents were not available
            // NOTE: The underlying buffer in `channel` does actually contain the data, we just cannot address it
            // because of the limits at the time of the creation.
            assertThat(channel).hasSize(0L);
        }
    }

    @Test
    public void inputStreamSnapshot() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            InputStream inputStream = out.getInputStream();

            out.write(1);
            assertThat(out.getInputStream().skip(1L)).isEqualTo(1L);

            assertThat(inputStream.skip(1L)).isEqualTo(0L);
        }
    }

    @Test
    public void byteSourceLiveView() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            ByteSource byteSource = out.asByteSource();

            assertThat(byteSource.sizeIfKnown()).hasValue(0L);
            out.write(1);
            assertThat(byteSource.sizeIfKnown()).hasValue(1L);
        }
    }

    @Test
    public void byteSourceAlreadyBuffered() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            ByteSource byteSource = out.asByteSource();

            Class<?> expectedType = out.getInputStream().getClass();
            assertThat(byteSource.openStream()).isInstanceOf(expectedType);
            assertThat(byteSource.openBufferedStream()).isInstanceOf(expectedType);
        }
    }

}