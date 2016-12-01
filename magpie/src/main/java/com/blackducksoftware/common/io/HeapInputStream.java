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

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * An extension of the standard byte array input stream that exposes more ways to access the content.
 *
 * @author jgustie
 */
public class HeapInputStream extends ByteArrayInputStream {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * The super class is a stream and doesn't need to keep track where the initial position in the buffer is. To expose
     * a seekable view of the contents we need to track this value on our own.
     */
    private int offset;

    public HeapInputStream(byte[] buf) {
        this(buf, 0, buf.length);
    }

    public HeapInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
        this.offset = offset;
    }

    public HeapInputStream(ByteBuffer buf) {
        super(EMPTY_BYTE_ARRAY);
        if (buf.hasArray()) {
            this.buf = buf.array();
            pos = offset = buf.arrayOffset() + buf.position();
            count = buf.arrayOffset() + buf.limit();
        } else {
            throw new IllegalArgumentException("buffer must be backed by an array");
        }
    }

    /**
     * Returns this input stream as a read-only channel. Does not copy the contents. If you require a writable channel,
     * create a new {@link HeapChannel} first; then use
     * {@link java.nio.channels.Channels#newInputStream(java.nio.channels.ReadableByteChannel) Channels.newInputStream}
     * to get the input stream.
     */
    public SeekableByteChannel getChannel() {
        try {
            @SuppressWarnings("resource")
            HeapChannel heapChannel = new HeapChannel(buf, offset, count);
            return heapChannel.position(pos - offset);
        } catch (ClosedChannelException e) {
            // We just created the channel, it can't be closed
            throw new IllegalStateException(e);
        }
    }

}
