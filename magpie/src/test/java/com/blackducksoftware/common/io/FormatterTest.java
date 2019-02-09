/*
 * Copyright 2019 Synopsys, Inc.
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

import static com.blackducksoftware.common.base.ExtraStrings.padBoth;
import static com.blackducksoftware.common.base.ExtraStrings.truncateEnd;
import static com.blackducksoftware.common.base.ExtraStrings.truncateMiddle;
import static com.blackducksoftware.common.base.ExtraStrings.truncateStart;
import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.padStart;
import static com.google.common.truth.Truth.assertThat;

import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.common.base.ExtraUUIDs;

/**
 * Tests for {@code Formatter}.
 *
 * @author jgustie
 */
public class FormatterTest {

    private static FooBar HELLO_WORLD = new FooBar("Hello World", UUID.randomUUID(), 1);

    private static FooBar BASIC_EMPTY = new FooBar("", ExtraUUIDs.nilUUID(), 0);

    @Test(expected = IllegalArgumentException.class)
    public void missingPrettyFormat() {
        new Formatter<>().format(HELLO_WORLD, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPlaceholder() {
        new Formatter<>().format(HELLO_WORLD, "format:%f");
    }

    @Test
    public void constantFormat() {
        Formatter<FooBar> formatter = new Formatter<>();
        formatter.setPrettyFormat("test", "Goodbye");
        assertThat(formatter.format(HELLO_WORLD, "format:")).isEqualTo("");
        assertThat(formatter.format(HELLO_WORLD, "tformat:")).isEqualTo("\n");
        assertThat(formatter.format(HELLO_WORLD, "format:Goodbye")).isEqualTo("Goodbye");
        assertThat(formatter.format(HELLO_WORLD, "test")).isEqualTo("Goodbye");
    }

    @Test
    public void standardPlaceholders() {
        Formatter<FooBar> formatter = new Formatter<>();
        assertThat(formatter.format(HELLO_WORLD, "format:%%")).isEqualTo("%");
        assertThat(formatter.format(HELLO_WORLD, "format:%n")).isEqualTo("\n");
        assertThat(formatter.format(HELLO_WORLD, "format:%x21")).isEqualTo("!");
    }

    @Test
    public void placeholderFlags() {
        Formatter<FooBar> formatter = new Formatter<>();
        formatter.setPlaceholder('f', FooBar::getFoo);
        assertThat(formatter.format(HELLO_WORLD, "format:'% f'")).isEqualTo("' Hello World'");
        assertThat(formatter.format(BASIC_EMPTY, "format:'% f'")).isEqualTo("''");
        assertThat(formatter.format(HELLO_WORLD, "format:'%+f'")).isEqualTo("'\nHello World'");
        assertThat(formatter.format(BASIC_EMPTY, "format:'%+f'")).isEqualTo("''");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void placeholderFlags_minusNotSupported() {
        Formatter<FooBar> formatter = new Formatter<>();
        formatter.setPlaceholder('f', FooBar::getFoo);
        formatter.format(HELLO_WORLD, "format:'%-f'");
    }

    @Test
    public void string() {
        Formatter<FooBar> formatter = new Formatter<>();
        formatter.setPlaceholder('f', FooBar::getFoo);
        assertThat(formatter.format(HELLO_WORLD, "format:%f")).isEqualTo(HELLO_WORLD.getFoo());
    }

    @Test
    public void color() {
        Formatter<FooBar> formatter = new Formatter<>();
        formatter.setPlaceholder('f', FooBar::getFoo);

        // We need to use '%C(always)' because we won't have a terminal in the tests
        assertThat(formatter.format(HELLO_WORLD, "format:%C(always)%Cred%f%Creset")).isEqualTo("\033[31mHello World\033[0m");

        // But even with '%C(always)' we can still go back to TTY detection
        assertThat(formatter.format(HELLO_WORLD, "format:%C(always)%C(auto)%Cred%f%Creset")).isEqualTo(HELLO_WORLD.getFoo());

        // Red by hex
        assertThat(formatter.format(HELLO_WORLD, "format:%C(always)%C(#FF0000)%f%Creset")).isEqualTo("\033[38;2;255;0;0mHello World\033[0m");

        // Red on green (so I can't see it!)
        assertThat(formatter.format(HELLO_WORLD, "format:%C(always)%C(red,green)%f%Creset"))
                .isEqualTo("\033[31m\033[42mHello World\033[0m");
        assertThat(formatter.format(HELLO_WORLD, "format:%C(always)%C(#FF0000,#00FF00)%f%Creset"))
                .isEqualTo("\033[38;2;255;0;0m\033[48;2;0;255;0mHello World\033[0m");

        // Bold
        assertThat(formatter.format(HELLO_WORLD, "format:%C(always)%C(bold)%f%C(nobold)")).isEqualTo("\033[1mHello World\033[21m");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidColorOption() {
        new Formatter<>().format(HELLO_WORLD, "format:%Cblack");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooManyColors() {
        new Formatter<>().format(HELLO_WORLD, "format:%C(red,green,blue)");
    }

    @Test
    public void padding() {
        Formatter<FooBar> formatter = new Formatter<>();
        formatter.setPlaceholder('f', FooBar::getFoo);
        assertThat(formatter.format(HELLO_WORLD, "format:%>(15)%f")).isEqualTo(padStart(HELLO_WORLD.getFoo(), 15, ' '));
        assertThat(formatter.format(HELLO_WORLD, "format:%<(15)%f")).isEqualTo(padEnd(HELLO_WORLD.getFoo(), 15, ' '));
        assertThat(formatter.format(HELLO_WORLD, "format:%><(15)%f")).isEqualTo(padBoth(HELLO_WORLD.getFoo(), 15, ' '));
        assertThat(formatter.format(HELLO_WORLD, "format:%>(10,trunc)%f")).isEqualTo(truncateEnd(HELLO_WORLD.getFoo(), 10));
        assertThat(formatter.format(HELLO_WORLD, "format:%>(10,ltrunc)%f")).isEqualTo(truncateStart(HELLO_WORLD.getFoo(), 10));
        assertThat(formatter.format(HELLO_WORLD, "format:%>(10,mtrunc)%f")).isEqualTo(truncateMiddle(HELLO_WORLD.getFoo(), 10));
    }

    /**
     * Class for testing formatter support.
     */
    public static class FooBar {
        private final String foo;

        private final UUID bar;

        private final int gus;

        public FooBar(String foo, UUID bar, int gus) {
            this.foo = foo;
            this.bar = bar;
            this.gus = gus;
        }

        public String getFoo() {
            return foo;
        }

        public UUID getBar() {
            return bar;
        }

        public int getGus() {
            return gus;
        }
    }

}
