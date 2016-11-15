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
package com.blackducksoftware.common.test;

import static com.blackducksoftware.common.test.ByteBufferSubject.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * Tests for the {@link ByteBufferSubject}.
 *
 * @author jgustie
 */
public class ByteBufferSubjectTest {

    @Test
    public void hasBytesSimple() {
        ByteBuffer subject = ByteBuffer.wrap(new byte[] { 0x01, 0x02, 0x03 });
        assertThat(subject).hasNextBytes((byte) 0x01, (byte) 0x02, (byte) 0x03);
    }

}
