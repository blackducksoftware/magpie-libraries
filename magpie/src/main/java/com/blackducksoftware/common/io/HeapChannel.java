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

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;

/**
 * A channel that is backed by a byte array.
 *
 * @author jgustie
 */
public class HeapChannel implements SeekableByteChannel {

    private final Set<StandardOpenOption> openOptions;

    private byte[] buf;

    private int off;

    private int pos;

    private int count;

    /**
     * Creates a new channel with the specified initial buffer size.
     */
    @Beta
    public HeapChannel(int size) {
        openOptions = EnumSet.of(StandardOpenOption.READ, StandardOpenOption.WRITE);
        buf = new byte[size];
    }

    /**
     * @see #HeapChannel(byte[], int, int)
     */
    public HeapChannel(byte[] buf) {
        this(buf, 0, buf.length);
    }

    /**
     * Creates a read-only channel from the supplied byte range.
     */
    public HeapChannel(byte[] buf, int off, int len) {
        openOptions = EnumSet.of(StandardOpenOption.READ);
        this.buf = buf;
        this.off = off;
        count = Math.min(Ints.saturatedCast((long) off + (long) len), buf.length);
    }

    @Override
    public boolean isOpen() {
        return !openOptions.isEmpty();
    }

    @Override
    public void close() {
        openOptions.clear();
    }

    @Override
    public int read(ByteBuffer dst) throws ClosedChannelException {
        Objects.requireNonNull(dst);
        requireOpen(StandardOpenOption.READ);
        if (pos < count) {
            int len = Math.min(dst.remaining(), count - off - pos);
            dst.put(buf, off + pos, len);
            pos += len;
            return len;
        } else {
            return -1;
        }
    }

    @Override
    public int write(ByteBuffer src) throws ClosedChannelException, NonWritableChannelException {
        Objects.requireNonNull(src);
        requireOpen(StandardOpenOption.WRITE);
        int start = pos;
        // TODO Int overflow
        int end = start + src.remaining();
        if (end > buf.length) {
            buf = Arrays.copyOf(buf, end);
        }
        src.get(buf, start, end - start);
        pos = end;
        count = Math.max(count, end);
        return end - start;
    }

    @Override
    public long position() throws ClosedChannelException {
        requireOpen();
        return pos;
    }

    @Override
    public HeapChannel position(long newPosition) throws ClosedChannelException {
        requireOpen();
        if (newPosition < 0L) {
            throw new IllegalArgumentException("newPosition must be non-negative");
        } else if (newPosition < Integer.MAX_VALUE) {
            pos = (int) newPosition;
        } else {
            pos = Integer.MAX_VALUE;
        }
        return this;
    }

    @Override
    public long size() throws ClosedChannelException {
        requireOpen();
        return count - off;
    }

    @Override
    public HeapChannel truncate(long size) throws ClosedChannelException {
        requireOpen(StandardOpenOption.WRITE);
        if (size < 0L) {
            throw new IllegalArgumentException("size must be non-negative");
        } else if (size < Integer.MAX_VALUE) {
            count = Math.min((int) size, buf.length - off);
        } else {
            count = buf.length - off;
        }
        if (pos > count) {
            pos = count;
        }
        return this;
    }

    private void requireOpen(StandardOpenOption... options)
            throws ClosedChannelException, NonReadableChannelException, NonWritableChannelException {
        if (openOptions.isEmpty()) {
            throw new ClosedChannelException();
        } else {
            for (StandardOpenOption option : options) {
                if (!openOptions.contains(option)) {
                    switch (option) {
                    case READ:
                        throw new NonReadableChannelException();
                    case WRITE:
                        throw new NonWritableChannelException();
                    default:
                        throw new IllegalStateException("unsupported open option: " + option);
                    }
                }
            }
        }
    }
}
