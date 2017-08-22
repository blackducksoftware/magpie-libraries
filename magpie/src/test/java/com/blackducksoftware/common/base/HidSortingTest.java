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

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the HID class sorting.
 *
 * @author jgustie
 */
public class HidSortingTest {

    @Test
    public void simpleFlatHierarchy() {
        assertThat(Arrays.asList(
                HID.from(Paths.get("/foo")),
                HID.from(Paths.get("/foo/bar")),
                HID.from(Paths.get("/foo/bar/gus")),
                HID.from(Paths.get("/foo/bar/gus/file1.txt"))))
                        .isStrictlyOrdered(HID.preOrder());
    }

    @Test
    public void simpleHierarchy() {
        // This example is from the Javadoc, so it better work
        assertThat(Arrays.asList(
                HID.from(Paths.get("/h")),
                HID.from(Paths.get("/h/d")),
                HID.from(Paths.get("/h/d/a")),
                HID.from(Paths.get("/h/d/b")),
                HID.from(Paths.get("/h/d/c")),
                HID.from(Paths.get("/h/e")),
                HID.from(Paths.get("/h/f")),
                HID.from(Paths.get("/h/f/g"))))
                        .isStrictlyOrdered(HID.preOrder());
    }

    @Test
    public void caseSensitivity() {
        assertThat(Arrays.asList(
                HID.from(Paths.get("/A")),
                HID.from(Paths.get("/B")),
                HID.from(Paths.get("/C")),
                HID.from(Paths.get("/D")),
                HID.from(Paths.get("/a")),
                HID.from(Paths.get("/b")),
                HID.from(Paths.get("/c")),
                HID.from(Paths.get("/d"))))
                        .isStrictlyOrdered(HID.preOrder());
    }

    @Test
    public void spaces() {
        assertThat(Arrays.asList(
                HID.from(Paths.get("/aa aa")),
                HID.from(Paths.get("/aaa aaa")),
                HID.from(Paths.get("/aaaa aaaa")),
                HID.from(Paths.get("/aaaaa aaaaa"))))
                        .isStrictlyOrdered(HID.preOrder());
    }

    @Test
    public void differentSegmentLength() {
        assertThat(HID.preOrder().compare(HID.from(Paths.get("/a/b/c/d/e")), HID.from(Paths.get("/a")))).isGreaterThan(0);
        assertThat(HID.preOrder().compare(HID.from(Paths.get("/a")), HID.from(Paths.get("/a/b/c/d/e")))).isLessThan(0);

        assertThat(HID.preOrder().compare(HID.from(Paths.get("/b/c/d/e")), HID.from(Paths.get("/a")))).isGreaterThan(0);
        assertThat(HID.preOrder().compare(HID.from(Paths.get("/a")), HID.from(Paths.get("/b/c/d/e")))).isLessThan(0);

        assertThat(HID.preOrder().compare(HID.from(Paths.get("/a/b/c/d/e")), HID.from(Paths.get("/b")))).isLessThan(0);
        assertThat(HID.preOrder().compare(HID.from(Paths.get("/b")), HID.from(Paths.get("/a/b/c/d/e")))).isGreaterThan(0);
    }

    @Test
    @Ignore("This type of sorting still needs to be implemented")
    public void numeric() {
        assertThat(Arrays.asList(
                HID.from(Paths.get("/0")),
                HID.from(Paths.get("/1")),
                HID.from(Paths.get("/2")),
                HID.from(Paths.get("/3")),
                HID.from(Paths.get("/4")),
                HID.from(Paths.get("/5")),
                HID.from(Paths.get("/6")),
                HID.from(Paths.get("/7")),
                HID.from(Paths.get("/8")),
                HID.from(Paths.get("/9")),
                HID.from(Paths.get("/10"))))
                        .isStrictlyOrdered(HID.preOrder());
    }

}
