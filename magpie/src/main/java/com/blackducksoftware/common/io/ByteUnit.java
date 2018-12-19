/*
 * Copyright 2018 Synopsys, Inc.
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

/**
 * A base interface for binary multipliers that provides a utility method for converting back to a number of bytes.
 *
 * @author jgustie
 */
public interface ByteUnit {

    /**
     * Equivalent to {@code BYTES.convert(count, this)}.
     *
     * @param count
     *            the byte count
     * @return the converted byte count, or {@code Long.MIN_VALUE} if conversion would negatively overflow, or
     *         {@code Long.MAX_VALUE} if it would positively overflow.
     */
    long toBytes(long count);

}
