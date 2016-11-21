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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Objects;

import com.google.common.io.ByteStreams;

/**
 * Extra I/O utilities.
 *
 * @author jgustie
 */
public class ExtraIO {

    /**
     * Creates a new {@link PrintWriter} using an explicit character encoding.
     */
    public static PrintWriter newPrintWriter(OutputStream out, Charset cs) {
        return new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, cs)));
    }

    /**
     * Conditionally buffers an input stream, returning the supplied stream if buffering is not necessary.
     */
    public static InputStream buffer(InputStream in) {
        Objects.requireNonNull(in);
        if (in instanceof BufferedInputStream
                || in instanceof ByteArrayInputStream) {
            return in;
        } else {
            return new BufferedInputStream(in);
        }
    }

    /**
     * Conditionally buffers an output stream, returning the supplied stream if buffering is not necessary.
     */
    public static OutputStream buffer(OutputStream out) {
        Objects.requireNonNull(out);
        if (out instanceof BufferedOutputStream
                || out instanceof ByteArrayOutputStream
                || out == ByteStreams.nullOutputStream()) {
            return out;
        } else {
            return new BufferedOutputStream(out);
        }
    }

    private ExtraIO() {
        assert false;
    }

}
