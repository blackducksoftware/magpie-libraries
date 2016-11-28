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

import static com.blackducksoftware.common.test.ByteBufferSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import org.junit.Test;

/**
 * Tests for {@link HeapInputStream}.
 *
 * @author jgustie
 */
public class HeapInputStreamTest {

    @Test(expected = NonWritableChannelException.class)
    public void readOnlyChannel() throws IOException {
        try (HeapInputStream in = new HeapInputStream(new byte[] { 0x00 })) {
            try (SeekableByteChannel channel = in.getChannel()) {
                channel.write(ByteBuffer.wrap(new byte[] { 0x00 }));
            }
        }
    }

    @Test
    public void channelPositionIndependent() throws IOException {
        try (HeapInputStream in = new HeapInputStream(new byte[] { 0x0F })) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            assertThat(in.getChannel().read(buffer)).isEqualTo(1);
            buffer.flip();
            assertThat(buffer).hasNextBytes((byte) 0x0F);

            assertThat(in.read()).isEqualTo(0x0F);
        }
    }

    // TODO Test this past an internal buffer resize
    @Test
    public void seekableBack() throws IOException {
        try (HeapInputStream in = new HeapInputStream(new byte[] { 0x0F })) {
            assertThat(in.read()).isEqualTo(0x0F);
            assertThat(in.read()).isEqualTo(-1);

            // Even though the content was read through the stream,
            // we can still set the position back on the channel
            SeekableByteChannel channel = in.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(2);
            assertThat(channel.read(buffer)).isEqualTo(-1);
            assertThat(channel.position(0).read(buffer)).isEqualTo(1);
            buffer.flip();
            assertThat(buffer).hasNextBytes((byte) 0x0F);
        }
    }

    @Test
    public void newHeapInputStream_offsetLimit() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapInputStream in = new HeapInputStream(data, 1, 2)) {
            assertThat(in.read()).isEqualTo(0x02);
            assertThat(in.read()).isEqualTo(0x03);
            assertThat(in.read()).isEqualTo(-1);
        }
    }

    @Test
    public void newHeapInputStream_fromBuffer_arrayOffsetLimit() throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03, 0x04 }, 1, 2);
        try (HeapInputStream in = new HeapInputStream(buffer)) {
            assertThat(in.read()).isEqualTo(0x02);
            assertThat(in.read()).isEqualTo(0x03);
            assertThat(in.read()).isEqualTo(-1);
        }
    }

    @Test
    public void newHeapInputStream_fromBuffer() throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { 0x02, 0x03 });
        try (HeapInputStream in = new HeapInputStream(buffer)) {
            assertThat(in.read()).isEqualTo(0x02);
            assertThat(in.read()).isEqualTo(0x03);
            assertThat(in.read()).isEqualTo(-1);
        }
    }

}
