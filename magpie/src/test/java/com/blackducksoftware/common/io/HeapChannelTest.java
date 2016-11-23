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
import static com.blackducksoftware.common.test.SeekableByteChannelSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;

import org.junit.Test;

/**
 * Tests for the {@link HeapChannel}.
 *
 * @author jgustie
 */
public class HeapChannelTest {

    /**
     * Verify we behave correctly when closed.
     */
    @Test
    public void closing() throws IOException {
        assertThat(new HeapChannel(0)).hasIdempotentClose();
        assertThat(new HeapChannel(0)).failsWhenClosed();
    }

    /**
     * Specifying a length to the constructor limits the available data.
     */
    @Test
    public void newHeapChannel_length() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data, 0, 2)) {
            assertThat(c).hasSize(2L);
            assertThat(c).isPositionedAt(0L);

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
            assertThat(c.read(buffer)).isEqualTo(2);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[0], data[1]);
        }
    }

    /**
     * Specifying both an offset and an length limits the available data.
     */
    @Test
    public void newHeapChannel_offsetLength() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data, 1, 2)) {
            assertThat(c.size()).isEqualTo(2);
            assertThat(c.position()).isEqualTo(0);

            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            assertThat(c.read(buffer)).isEqualTo(2);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[1], data[2]);
        }
    }

    /**
     * Specifying a length greater then the supplied buffer size should still work.
     */
    @Test
    public void newHeapChannel_offsetExcessiveLength() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data, 1, data.length + 1)) {
            assertThat(c.size()).isEqualTo(3);
            assertThat(c.position()).isEqualTo(0);

            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            assertThat(c.read(buffer)).isEqualTo(3);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[1], data[2], data[3]);
        }
    }

    /**
     * Specifying a length of {@value Integer#MAX_VALUE} should not overflow.
     */
    @Test
    public void newHeapChannel_offsetMaxLength() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data, 1, Integer.MAX_VALUE)) {
            assertThat(c.size()).isEqualTo(3);
            assertThat(c.position()).isEqualTo(0);

            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            assertThat(c.read(buffer)).isEqualTo(3);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[1], data[2], data[3]);
        }
    }

    /**
     * A byte array with no offset or limit is fully available.
     */
    @Test
    public void newHeapChannel_byteArray() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data)) {
            assertThat(c).hasSize(data.length);
            assertThat(c).isPositionedAt(0L);

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
            assertThat(c.read(buffer)).isEqualTo(data.length);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data);
        }
    }

    /**
     * If the backing array is passed in, the channel is read only.
     */
    @Test(expected = NonWritableChannelException.class)
    public void newHeapChannel_nonWritable() throws IOException {
        try (HeapChannel c = new HeapChannel(new byte[1])) {
            c.write(ByteBuffer.wrap(new byte[] { 0 }));
        }
    }

    /**
     * Negative position is illegal.
     */
    @Test(expected = IllegalArgumentException.class)
    public void positionNegative() throws IOException {
        try (HeapChannel c = new HeapChannel(0)) {
            c.position(-1L);
        }
    }

    /**
     * Position is bounded to {@value Integer#MAX_VALUE}.
     */
    @Test
    public void positionIntegerMax() throws IOException {
        try (HeapChannel c = new HeapChannel(0)) {
            c.position(Long.MAX_VALUE);
            assertThat(c).isPositionedAt(Integer.MAX_VALUE);
        }
    }

    /**
     * The position is preserved, even if greater then the channel size.
     */
    @Test
    public void positionPastSize() throws IOException {
        try (HeapChannel c = new HeapChannel(0)) {
            c.position(1001L);
            assertThat(c).isPositionedAt(1001L);
        }
    }

    /**
     * Reading to a {@code null} buffer fails.
     */
    @Test(expected = NullPointerException.class)
    public void readNull() throws IOException {
        try (HeapChannel c = new HeapChannel(0)) {
            c.read(null);
        }
    }

    /**
     * Positioning past the size should result in reads reporting EOF.
     */
    @Test
    public void readAfterPositionOutOfRange() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data)) {
            c.position(4);

            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            assertThat(c.read(buffer)).isEqualTo(-1);
            assertThat(buffer).hasRemaining(data.length);
        }
    }

    /**
     * Reading should resume relative to the current position.
     */
    @Test
    public void readAfterPositionInRange() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data)) {
            c.position(1);

            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            assertThat(c.read(buffer)).isEqualTo(3);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[1], data[2], data[3]);
        }
    }

    /**
     * Writing a {@code null} buffer fails.
     */
    @Test(expected = NullPointerException.class)
    public void writeNull() throws IOException {
        try (HeapChannel c = new HeapChannel(0)) {
            c.write(null);
        }
    }

    /**
     * If we reposition, we can read back everything we wrote.
     */
    @Test
    public void writeReadBack() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data.length)) {
            assertThat(c.write(ByteBuffer.wrap(data))).isEqualTo(data.length);
            assertThat(c.read(ByteBuffer.allocate(1))).isEqualTo(-1);
            c.position(0L);

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
            assertThat(c.read(buffer)).isEqualTo(data.length);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data);
        }
    }

    /**
     * If we write twice, we can read back everything as one contiguous buffer.
     */
    @Test
    public void writeSplitReadBack() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data.length)) {
            assertThat(c.write(ByteBuffer.wrap(data, 0, 2))).isEqualTo(2);
            assertThat(c.write(ByteBuffer.wrap(data, 2, 2))).isEqualTo(2);
            c.position(0L);

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
            assertThat(c.read(buffer)).isEqualTo(data.length);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data);
        }
    }

    /**
     * If we read twice, reads pick up at the previous position.
     */
    @Test
    public void writeReadSplitBack() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(data.length)) {
            assertThat(c.write(ByteBuffer.wrap(data))).isEqualTo(data.length);
            assertThat(c.read(ByteBuffer.allocate(1))).isEqualTo(-1);
            c.position(0L);

            ByteBuffer buffer = ByteBuffer.allocate(2);
            assertThat(c.read(buffer)).isEqualTo(2);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[0], data[1]);
            assertThat(c.read(buffer)).isEqualTo(2);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data[2], data[3]);
        }
    }

    /**
     * If we write past the current size, the channel's buffer grows to accommodate the additional contents.
     */
    @Test
    public void writeGrow() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(2)) {
            assertThat(c.size()).isEqualTo(0);
            assertThat(c.write(ByteBuffer.wrap(data))).isEqualTo(data.length);
            assertThat(c.size()).isEqualTo(4);
            assertThat(c.read(ByteBuffer.allocate(1))).isEqualTo(-1);
            c.position(0L);

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
            assertThat(c.read(buffer)).isEqualTo(data.length);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data);
        }
    }

    /**
     * If we write to a non-empty channel, the channel's buffer still grows.
     */
    @Test
    public void writeSplitGrow() throws IOException {
        byte[] data = new byte[] { 0x01, 0x02, 0x03, 0x04 };
        try (HeapChannel c = new HeapChannel(2)) {
            assertThat(c.write(ByteBuffer.wrap(data, 0, 1))).isEqualTo(1);
            assertThat(c.write(ByteBuffer.wrap(data, 1, 3))).isEqualTo(3);
            c.position(0L);

            ByteBuffer buffer = ByteBuffer.allocate(data.length + 1);
            assertThat(c.read(buffer)).isEqualTo(data.length);
            buffer.flip();
            assertThat(buffer).hasNextBytes(data);
        }
    }

}
