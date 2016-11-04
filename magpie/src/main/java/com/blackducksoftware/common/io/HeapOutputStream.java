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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * An extension of the standard byte array output stream that exposes more ways to access the written content.
 *
 * @author jgustie
 */
public class HeapOutputStream extends ByteArrayOutputStream {

    public HeapOutputStream() {
        super();
    }

    public HeapOutputStream(int size) {
        super(size);
    }

    /**
     * Checks to see if the capacity of this buffer is large enough to contain the supplied size.
     */
    public boolean hasCapacity(long size) {
        return size <= buf.length;
    }

    /**
     * Transfers the contents written to this output stream into another writable channel.
     */
    public synchronized void transferTo(WritableByteChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(buf, 0, count).asReadOnlyBuffer());
    }

    /**
     * Creates a new seekable channel from the current contents written to this output stream.
     */
    public synchronized SeekableByteChannel getChannel() {
        return new HeapChannel(buf, 0, count);
    }

    /**
     * Creates a new input stream from the current contents written to this output stream.
     */
    public synchronized InputStream getInputStream() {
        return new HeapInputStream(buf, 0, count);
    }

}
