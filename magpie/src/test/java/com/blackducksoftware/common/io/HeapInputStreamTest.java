/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
