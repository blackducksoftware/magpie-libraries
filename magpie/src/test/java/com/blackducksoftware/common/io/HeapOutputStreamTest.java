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

import static com.blackducksoftware.test.common.ByteBufferSubject.assertThat;
import static com.blackducksoftware.test.common.SeekableByteChannelSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Base64;

import org.junit.Test;

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

    @Test
    public void transferFrom() throws IOException {
        try (HeapOutputStream out1 = new HeapOutputStream()) {
            try (HeapOutputStream out2 = new HeapOutputStream()) {
                out1.write(1);
                out1.write(2);
                out2.write(3);
                out2.write(4);
                out1.transferFrom(out2);
                assertThat(out1.toByteBuffer()).hasNextBytes((byte) 1, (byte) 2, (byte) 3, (byte) 4);
            }
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
    public void base64encode() throws IOException {
        try (HeapOutputStream out = new HeapOutputStream()) {
            out.write("Hello World".getBytes(US_ASCII));
            assertThat(out.toString(Base64.getEncoder())).isEqualTo("SGVsbG8gV29ybGQ=");
        }
    }

}
