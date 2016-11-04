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

import java.net.URI;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * Tests for the name normalization of the HID class.
 *
 * @author jgustie
 */
public class HidNormalizationTest {

    @Test
    public void emptyPath() {
        // Here are a bunch of strange ways to create "empty" paths
        assertThat(HID.from(URI.create("file:///")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:///.")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/.")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/./")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/..")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/../")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/../..")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/../../")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#.")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#/")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#/.")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#/./")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#/../")).getPath()).isEqualTo("/");
        assertThat(HID.from(Paths.get("/")).getPath()).isEqualTo("/");
        assertThat(HID.from(Paths.get("/.")).getPath()).isEqualTo("/");
    }

    @Test
    public void oneSegmentPath() {
        assertThat(HID.from(URI.create("file:/a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("file:/./a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("file:/a/")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#/a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#./a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#/./a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(Paths.get("/a")).getPath()).isEqualTo("/a");
    }

    @Test
    public void dotNormalization() {
        assertThat(HID.from(URI.create("file:/..")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/../..")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/a/..")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/a/b/..")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("file:/a/b/../..")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:/a/b/../../..")).getPath()).isEqualTo("/");

        // TODO This is strange case
        assertThat(HID.from(URI.create("zip:file:%2F#..")).getPath()).isEqualTo("/..");
        assertThat(HID.from(URI.create("zip:file:%2F#../")).getPath()).isEqualTo("/..");
    }

    @Test
    public void slashes() {
        assertThat(HID.from(URI.create("file:/")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:///")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file:////")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file://///")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("file://////")).getPath()).isEqualTo("/");

        assertThat(HID.from(URI.create("zip:file:%2F#")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#/")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#//")).getPath()).isEqualTo("/");
        assertThat(HID.from(URI.create("zip:file:%2F#///")).getPath()).isEqualTo("/");

        assertThat(HID.from(URI.create("zip:file:%2F#a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#/a")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#/a/")).getPath()).isEqualTo("/a");
        assertThat(HID.from(URI.create("zip:file:%2F#a/")).getPath()).isEqualTo("/a");
    }

    @Test
    public void combiningCharacter() {
        HID nfc = HID.from(URI.create("file:/f%C3%B6%C3%B6"));
        HID nfd = HID.from(URI.create("file:/fo%CC%88o%CC%88"));

        assertThat(nfc.getPath()).isEqualTo("/f\u00F6\u00F6");
        assertThat(nfd.getPath()).isEqualTo("/f\u00F6\u00F6");
        assertThat(nfc).isEqualTo(nfd);
    }

}
