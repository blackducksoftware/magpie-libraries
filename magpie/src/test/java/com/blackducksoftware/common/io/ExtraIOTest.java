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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.io.CountingOutputStream;

/**
 * Tests for {@link ExtraIO}.
 *
 * @author jgustie
 */
public class ExtraIOTest {

    @Test
    public void printWriterCharset() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = ExtraIO.newPrintWriter(out, Charset.forName("windows-1252"));
        pw.print("\u2122");
        pw.flush();
        assertThat(out.toByteArray()).asList().containsExactly((byte) 0x99);
    }

    @Test
    public void bufferInputStream() {
        // Note that the counting filter is enough to force re-buffering...
        InputStream in = new CountingInputStream(new ByteArrayInputStream(new byte[0]));
        InputStream buffered = ExtraIO.buffer(in);
        assertThat(buffered).isNotSameAs(in);
        assertThat(buffered).isInstanceOf(BufferedInputStream.class);
    }

    @Test(expected = NullPointerException.class)
    public void bufferInputStream_null() {
        ExtraIO.buffer((InputStream) null);
    }

    @Test
    public void bufferInputStream_alreadyBuffered() {
        InputStream in = new BufferedInputStream(null);
        assertThat(ExtraIO.buffer(in)).isSameAs(in);
    }

    @Test
    public void bufferInputStream_byteArrayInputStream() {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertThat(ExtraIO.buffer(in)).isSameAs(in);
    }

    @Test
    public void bufferInputStream_heapInputStream() {
        InputStream in = new HeapInputStream(new byte[0]);
        assertThat(ExtraIO.buffer(in)).isSameAs(in);
    }

    @Test
    public void bufferOutputStream() {
        // Note that the counting filter is enough to force re-buffering...
        OutputStream out = new CountingOutputStream(ByteStreams.nullOutputStream());
        OutputStream buffered = ExtraIO.buffer(out);
        assertThat(buffered).isNotSameAs(out);
        assertThat(buffered).isInstanceOf(BufferedOutputStream.class);
    }

    @Test(expected = NullPointerException.class)
    public void bufferOutputStream_null() {
        ExtraIO.buffer((OutputStream) null);
    }

    @Test
    public void bufferOutputStream_alreadyBuffered() {
        OutputStream out = new BufferedOutputStream(null);
        assertThat(ExtraIO.buffer(out)).isSameAs(out);
    }

    @Test
    public void bufferOutputStream_byteArrayOutputStream() {
        OutputStream out = new ByteArrayOutputStream();
        assertThat(ExtraIO.buffer(out)).isSameAs(out);
    }

    @Test
    public void bufferOutputStream_heapOutputStream() {
        OutputStream out = new HeapOutputStream();
        assertThat(ExtraIO.buffer(out)).isSameAs(out);
    }

    @Test
    public void bufferOutputStream_nullOutputStream() {
        OutputStream out = ByteStreams.nullOutputStream();
        assertThat(ExtraIO.buffer(out)).isSameAs(out);
    }

    @Test
    public void ignoreCloseInput() throws IOException {
        ExtraIO.ignoreClose(new InputStream() {
            @Override
            public int read() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException {
                assert_().fail();
            }
        }).close();
    }

    @Test
    public void ignoreCloseOutput() throws IOException {
        ExtraIO.ignoreClose(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() throws IOException {
                assert_().fail();
            }
        }).close();
    }

    @Test(expected = NullPointerException.class)
    public void onIdleNullIn() {
        ExtraIO.onIdle(null, 1, TimeUnit.MILLISECONDS, () -> {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void onIdleNegativeTimeout() {
        ExtraIO.onIdle(HeapInputStream.empty(), -1, TimeUnit.MILLISECONDS, () -> {});
    }

    @Test(expected = NullPointerException.class)
    public void onIdleNullUnit() {
        ExtraIO.onIdle(HeapInputStream.empty(), 1, null, () -> {});
    }

    @Test(expected = NullPointerException.class)
    public void onIdleNullCallback() {
        ExtraIO.onIdle(HeapInputStream.empty(), 1, TimeUnit.MILLISECONDS, null);
    }

    @Test
    public void onIdleTimeout() throws InterruptedException {
        AtomicBoolean called = new AtomicBoolean();
        InputStream in = ExtraIO.onIdle(HeapInputStream.empty(), 1, TimeUnit.MILLISECONDS, () -> called.set(true));
        // Do nothing, allow the timeout
        ExtraIO.joinOnIdleInputStream(in);
        assertThat(called.get()).isTrue();
    }

    @Test
    public void onIdleClose() throws IOException, InterruptedException {
        AtomicBoolean called = new AtomicBoolean();
        InputStream in = ExtraIO.onIdle(HeapInputStream.empty(), 1, TimeUnit.MILLISECONDS, () -> called.set(true));
        in.close();
        ExtraIO.joinOnIdleInputStream(in);
        assertThat(called.get()).isFalse();
    }

    @Test
    public void onIdleSkip() throws IOException, InterruptedException {
        AtomicBoolean called = new AtomicBoolean();
        InputStream in = ExtraIO.onIdle(HeapInputStream.empty(), 1, TimeUnit.MILLISECONDS, () -> called.set(true));
        in.skip(1);
        ExtraIO.joinOnIdleInputStream(in);
        assertThat(called.get()).isFalse();
    }

    @Test
    public void onIdleReadRange() throws IOException, InterruptedException {
        AtomicBoolean called = new AtomicBoolean();
        InputStream in = ExtraIO.onIdle(HeapInputStream.empty(), 1, TimeUnit.MILLISECONDS, () -> called.set(true));
        in.read(new byte[1]);
        ExtraIO.joinOnIdleInputStream(in);
        assertThat(called.get()).isFalse();
    }

    @Test
    public void onIdleReadSingle() throws IOException, InterruptedException {
        AtomicBoolean called = new AtomicBoolean();
        InputStream in = ExtraIO.onIdle(HeapInputStream.empty(), 1, TimeUnit.MILLISECONDS, () -> called.set(true));
        in.read();
        ExtraIO.joinOnIdleInputStream(in);
        assertThat(called.get()).isFalse();
    }

}
