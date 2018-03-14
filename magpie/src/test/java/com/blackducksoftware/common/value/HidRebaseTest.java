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
package com.blackducksoftware.common.value;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/**
 * Tests for the rebasing functionality of the HID value.
 *
 * @author jgustie
 */
public class HidRebaseTest {

    /**
     * If you pass the same two base HIDs you get a no-op, even if an error should have been raised because the rebase
     * would have been invalid. This same test fails if "file:/b" is changed to "file:/c" because "/b" is not an
     * ancestor of "/a", but because the equal base test short circuits even the preconditions, this still passes.
     */
    @Test
    public void shortCircuit() {
        assertThat(HID.from("file:/a").getRebased(HID.from("file:/b"), HID.from("file:/b")).getPath()).isEqualTo("/a");
    }

    @Test
    public void rebaseNesting0() {
        assertThat(HID.from("file:/h").getRebased(HID.from("file:/"), HID.from("file:/i")).getPath()).isEqualTo("/i/h");
        assertThat(HID.from("file:/h/e").getRebased(HID.from("file:/h"), HID.from("file:/")).getPath()).isEqualTo("/e");
        assertThat(HID.from("file:/h/e").getRebased(HID.from("file:/h"), HID.from("file:/i")).getPath()).isEqualTo("/i/e");
        assertThat(HID.from("file:/h/d/a").getRebased(HID.from("file:/h/d"), HID.from("file:/h/i")).getPath()).isEqualTo("/h/i/a");
        assertThat(HID.from("file:/h/d/a").getRebased(HID.from("file:/h/d"), HID.from("file:/h/i/j")).getPath()).isEqualTo("/h/i/j/a");
    }

    @Test
    public void rebaseNesting1() {
        HID a = new HID.Builder().push("file", "/foo/bar/gus.tar").push("tar", "/h/d/a").build();
        assertThat(a.getRebased(HID.from("file:/foo/bar"), HID.from("file:/foo/test")))
                .isEqualTo(new HID.Builder().push("file", "/foo/test/gus.tar").push("tar", "/h/d/a").build());
        assertThat(a.getRebased(HID.from("file:/foo/bar/gus.tar"), HID.from("file:/foo/bar/gus")))
                .isEqualTo(new HID.Builder().push("file", "/foo/bar/gus").push("tar", "/h/d/a").build());
        assertThat(a.getRebased(HID.from("tar:file:%2Ffoo%2Fbar%2Fgus.tar#/h/d"), HID.from("file:/foo/bar/h/d")))
                .isEqualTo(HID.from("file:/foo/bar/h/d/a"));
        assertThat(a.getRebased(HID.from("file:/foo/bar/gus.tar"), new HID.Builder().push("file", "/test.zip").push("zip", "/foo/bar/gus.tar").build()))
                .isEqualTo(new HID.Builder().push("file", "/test.zip").push("zip", "/foo/bar/gus.tar").push("tar", "/h/d/a").build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rebaseNonAncestorNesting0() {
        // The old base must be an ancestor of the original
        HID.from("file:/a").getRebased(HID.from("file:/b"), HID.from("file:/c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rebaseNesting1PartialPath() {
        // This isn't allowed because the bases do not have anything in common and old base doesn't match the entire
        // nesting level of the original
        new HID.Builder()
                .push("file", "/foo/bar/gus.tar")
                .push("tar", "/h/d/a")
                .build()
                .getRebased(HID.from("file:/foo/bar"), HID.from("file:/example"));
    }

}
