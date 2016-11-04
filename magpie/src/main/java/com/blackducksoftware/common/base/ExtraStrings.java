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
package com.blackducksoftware.common.base;

import java.util.Objects;

import javax.annotation.Nullable;

public class ExtraStrings {

    /**
     * Returns a string ensuring that it begins with the specified prefix, prepending it if necessary.
     */
    @Nullable
    public static String ensurePrefix(CharSequence prefix, @Nullable CharSequence value) {
        Objects.requireNonNull(prefix);
        if (value == null) {
            return null;
        } else if (startsWith(value, prefix, 0)) {
            return value.toString();
        } else {
            return new StringBuilder(prefix.length() + value.length()).append(prefix).append(value).toString();
        }
    }

    public static String ensureDelimiter(@Nullable CharSequence start, CharSequence delimiter, @Nullable CharSequence end) {
        Objects.requireNonNull(delimiter);
        if (start == null && end == null) {
            return delimiter.toString();
        }

        StringBuilder result = new StringBuilder((start != null ? start.length() : 0) + delimiter.length() + (end != null ? end.length() : 0));

        boolean delimit = true;
        if (start != null && startsWith(start, delimiter, start.length() - delimiter.length())) {
            delimit = false;
        }

        int endOffset = 0;
        if (end != null && startsWith(end, delimiter, 0)) {
            if (delimit) {
                delimit = false;
            } else {
                endOffset = delimiter.length();
            }
        }

        if (start != null) {
            result.append(start);
        }
        if (delimit) {
            result.append(delimiter);
        }
        if (end != null) {
            result.append(end, endOffset, end.length());
        }
        return result.toString();
    }

    private static boolean startsWith(CharSequence value, CharSequence prefix, int offset) {
        int o = prefix.length();
        int pi = 0;
        int vi = offset;
        if (offset < 0 || offset > value.length() - prefix.length()) {
            return false;
        }
        while (--o >= 0) {
            if (prefix.charAt(pi++) != value.charAt(vi++)) {
                return false;
            }
        }
        return true;
    }

    private ExtraStrings() {
        assert false;
    }
}
