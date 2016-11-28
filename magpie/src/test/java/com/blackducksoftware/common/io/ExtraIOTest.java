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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.io.CountingOutputStream;

/**
 * Tests for {link ExtraIO}.
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

}