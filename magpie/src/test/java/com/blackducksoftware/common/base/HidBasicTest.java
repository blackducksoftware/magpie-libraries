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
import static com.google.common.truth.TruthJUnit.assume;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Tests for the basic functionality of the HID class.
 *
 * @author jgustie
 */
public class HidBasicTest {

    @Test
    public void toUri() {
        URI originalUri = URI.create("file:///foo/bar/gus.txt");
        HID hid = HID.from(originalUri);
        URI firstUri = hid.toUri();
        URI secondURI = hid.toUri();

        assertThat(firstUri).isSameAs(secondURI);
        assertThat(firstUri).isNotSameAs(originalUri);
        assertThat(firstUri).isEqualTo(originalUri);
    }

    @Test
    public void fromUriUnknownScheme() {
        // For you URI junkies out there, one slash indicates hierarchy, two means host, three means no host
        assertThat(HID.from(URI.create("test:/foo/")).getPath()).isEqualTo("/foo");
        assertThat(HID.from(URI.create("test://foo/")).getPath()).isEqualTo("/"); // "foo" is the ignored host name!
        assertThat(HID.from(URI.create("test:///foo/")).getPath()).isEqualTo("/foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromUriUnknownSchemeFragment() {
        // If we don't recognize the scheme, we can't handle fragments
        HID.from(URI.create("test:/foo#bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromUriUnknownSchemeQuery() {
        // If we don't recognize the scheme, we can't handle queries
        HID.from(URI.create("test:/foo?bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromUriOpaque() {
        // An opaque URI doesn't have a hierarchy, so a HID doesn't make sense
        HID.from(URI.create("uuid:1a766934-e1a9-48af-962d-289c215f6726"));
    }

    @Test
    public void fromUriJavaJarArchive() {
        // Black Duck archive URI style is different from Java
        HID javaJarArchive = HID.from(URI.create("jar:file:///foo.zip!/bar.txt"));
        HID blackDuckJarArchive = HID.from(URI.create("jar:file:%2F%2F%2Ffoo.zip#/bar.txt"));

        // They should be the same
        assertThat(javaJarArchive).isEqualTo(blackDuckJarArchive);
        assertThat(javaJarArchive.getPath()).isEqualTo("/bar.txt");
    }

    @Test
    public void fromUriNesting0Depth0() {
        URI uri = URI.create("file:///");
        HID nesting0Depth0 = HID.from(uri);

        // Nesting and depth
        assertThat(nesting0Depth0.nesting()).isEqualTo(0);
        assertThat(nesting0Depth0.depth()).isEqualTo(0);

        // Round trip back to URI
        assertThat(nesting0Depth0.toUri()).isEqualTo(uri);

        // At depth == 0 the root should be us
        assertThat(nesting0Depth0.isRoot()).isTrue();
        assertThat(nesting0Depth0.getRoot()).isEqualTo(nesting0Depth0);

        // At nesting == 0 there is no container
        assertThat(nesting0Depth0.hasContainer()).isFalse();
        assertThat(nesting0Depth0.getContainer()).isNull();

        // getBase() for nesting == 0 should be equal to itself
        assertThat(nesting0Depth0.getBase()).isEqualTo(nesting0Depth0);

        // Parent at depth 0 is null
        assertThat(nesting0Depth0.getParent()).isNull();

        // At depth == 0 the path is the name
        assertThat(nesting0Depth0.getPath()).isEqualTo("/");

        // Name is not empty, it is a slash for the root
        assertThat(nesting0Depth0.getName()).isEqualTo("/");
    }

    @Test
    public void fromUriNesting0Depth3() {
        URI uri = URI.create("file:///foo/bar/gus.txt");
        HID nesting0Depth3 = HID.from(uri);

        // Nesting and depth
        assertThat(nesting0Depth3.nesting()).isEqualTo(0);
        assertThat(nesting0Depth3.depth()).isEqualTo(3);

        // Round trip back to URI
        assertThat(nesting0Depth3.toUri()).isEqualTo(uri);

        // At nesting == 0 the root is just first ancestor
        assertThat(nesting0Depth3.isRoot()).isFalse();
        assertThat(nesting0Depth3.getRoot()).isEqualTo(HID.from(URI.create("file:///")));

        // At nesting == 0 there is no container
        assertThat(nesting0Depth3.hasContainer()).isFalse();
        assertThat(nesting0Depth3.getContainer()).isNull();

        // getBase() for nesting == 0 should be equal to itself
        assertThat(nesting0Depth3.getBase()).isEqualTo(nesting0Depth3);

        // Parent at depth 3 is just the second to last segment
        assertThat(nesting0Depth3.getParent()).isEqualTo(HID.from(URI.create("file:///foo/bar")));

        // At nesting == 0 the path is pretty much the full URI
        assertThat(nesting0Depth3.getPath()).isEqualTo("/foo/bar/gus.txt");

        // Name is always the last segment
        assertThat(nesting0Depth3.getName()).isEqualTo("gus.txt");
    }

    @Test
    public void fromUriNesting1Depth0() {
        URI uri = URI.create("tar:file:%2F%2F%2Ffoo%2Fbar.tar#/");
        HID nesting1Depth0 = HID.from(uri);

        // Nesting and depth
        assertThat(nesting1Depth0.nesting()).isEqualTo(1);
        assertThat(nesting1Depth0.depth()).isEqualTo(0);

        // Round trip back to URI
        assertThat(nesting1Depth0.toUri()).isEqualTo(uri);

        // At depth == 0 the root should be us
        assertThat(nesting1Depth0.isRoot()).isTrue();
        assertThat(nesting1Depth0.getRoot()).isEqualTo(nesting1Depth0);

        // At nesting == 1 there the container is our archive
        assertThat(nesting1Depth0.hasContainer()).isTrue();
        assertThat(nesting1Depth0.getContainer()).isEqualTo(HID.from(URI.create("file:///foo/bar.tar")));

        // getBase() for nesting == 1 should be the container
        assertThat(nesting1Depth0.getBase()).isEqualTo(nesting1Depth0.getContainer());

        // Parent at nesting == 1 and depth == 0 should be the container
        assertThat(nesting1Depth0.getParent()).isEqualTo(nesting1Depth0.getContainer());

        // At depth == 0 the path is the name
        assertThat(nesting1Depth0.getPath()).isEqualTo("/");

        // Name is not empty, it is a slash for the root
        assertThat(nesting1Depth0.getName()).isEqualTo("/");
    }

    @Test
    public void fromUriNesting1Depth3() {
        URI uri = URI.create("tar:file:%2F%2F%2Ffoo%2Fbar.tar#/abc/def/ghi.txt");
        HID nesting1Depth3 = HID.from(uri);

        // Nesting and depth
        assertThat(nesting1Depth3.nesting()).isEqualTo(1);
        assertThat(nesting1Depth3.depth()).isEqualTo(3);

        // Round trip back to URI
        assertThat(nesting1Depth3.toUri()).isEqualTo(uri);

        // At nesting == 1 the root is still in the archive
        assertThat(nesting1Depth3.isRoot()).isFalse();
        assertThat(nesting1Depth3.getRoot()).isEqualTo(HID.from(URI.create("tar:file:%2F%2F%2Ffoo%2Fbar.tar#/")));

        // At nesting == 1 there the container is our archive
        assertThat(nesting1Depth3.hasContainer()).isTrue();
        assertThat(nesting1Depth3.getContainer()).isEqualTo(HID.from(URI.create("file:///foo/bar.tar")));

        // getBase() for nesting == 1 should be the container
        assertThat(nesting1Depth3.getBase()).isEqualTo(nesting1Depth3.getContainer());

        // Parent at depth 3 is just the second to last segment
        assertThat(nesting1Depth3.getParent()).isEqualTo(HID.from(URI.create("tar:file:%2F%2F%2Ffoo%2Fbar.tar#/abc/def")));

        // At nesting == 1 the path is pretty much the fragment
        assertThat(nesting1Depth3.getPath()).isEqualTo("/abc/def/ghi.txt");

        // Name is always the last segment
        assertThat(nesting1Depth3.getName()).isEqualTo("ghi.txt");
    }

    @Test
    public void isAncestorNesting0() {
        // This the tree from the pre-order sorting Javadoc
        HID a = HID.from(Paths.get("/h/d/a"));
        HID b = HID.from(Paths.get("/h/d/b"));
        HID c = HID.from(Paths.get("/h/d/c"));
        HID d = HID.from(Paths.get("/h/d"));
        HID e = HID.from(Paths.get("/h/e"));
        HID f = HID.from(Paths.get("/h/f"));
        HID g = HID.from(Paths.get("/h/f/g"));
        HID h = HID.from(Paths.get("/h"));

        // H is the ancestor of everything but itself
        assertThat(a.isAncestor(h)).isTrue();
        assertThat(b.isAncestor(h)).isTrue();
        assertThat(c.isAncestor(h)).isTrue();
        assertThat(d.isAncestor(h)).isTrue();
        assertThat(e.isAncestor(h)).isTrue();
        assertThat(f.isAncestor(h)).isTrue();
        assertThat(g.isAncestor(h)).isTrue();
        assertThat(h.isAncestor(h)).isFalse();

        // D is the ancestor of A, B and C
        assertThat(a.isAncestor(d)).isTrue();
        assertThat(b.isAncestor(d)).isTrue();
        assertThat(c.isAncestor(d)).isTrue();

        // F is the ancestor of G
        assertThat(g.isAncestor(f)).isTrue();

        // E is a leaf, ancestor of no one
        assertThat(a.isAncestor(e)).isFalse();
        assertThat(b.isAncestor(e)).isFalse();
        assertThat(c.isAncestor(e)).isFalse();
        assertThat(d.isAncestor(e)).isFalse();
        assertThat(e.isAncestor(e)).isFalse();
        assertThat(f.isAncestor(e)).isFalse();
        assertThat(g.isAncestor(e)).isFalse();
        assertThat(h.isAncestor(e)).isFalse();
    }

    @Test
    public void isAncestorNesting1() {
        HID a = HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h/d/a"));
        HID d = HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h/d"));
        HID e = HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h/e"));
        HID h = HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h"));
        HID x = HID.from(URI.create("file:/z/y/x"));
        HID y = HID.from(URI.create("file:/z/y"));
        HID z = HID.from(URI.create("file:/z"));

        // Z is the ancestor of everything but itself
        assertThat(a.isAncestor(z)).isTrue();
        assertThat(d.isAncestor(z)).isTrue();
        assertThat(h.isAncestor(z)).isTrue();
        assertThat(x.isAncestor(z)).isTrue();
        assertThat(y.isAncestor(z)).isTrue();
        assertThat(z.isAncestor(z)).isFalse();

        // Y is the ancestor of everything but itself and z
        assertThat(a.isAncestor(y)).isTrue();
        assertThat(d.isAncestor(y)).isTrue();
        assertThat(h.isAncestor(y)).isTrue();
        assertThat(x.isAncestor(z)).isTrue();
        assertThat(y.isAncestor(y)).isFalse();
        assertThat(z.isAncestor(y)).isFalse();

        // Not even in the same ball park
        assertThat(x.isAncestor(a)).isFalse();
        assertThat(y.isAncestor(a)).isFalse();
        assertThat(z.isAncestor(a)).isFalse();

        // H is still the ancestor of A and D
        assertThat(a.isAncestor(h)).isTrue();
        assertThat(d.isAncestor(h)).isTrue();

        // E is still not an ancestor
        assertThat(a.isAncestor(e)).isFalse();
        assertThat(d.isAncestor(e)).isFalse();
        assertThat(e.isAncestor(e)).isFalse();
        assertThat(h.isAncestor(e)).isFalse();
    }

    @Test
    public void relativizedPath() {
        HID z = HID.from(URI.create("file:/z"));
        HID x = HID.from(URI.create("file:/z/y/x"));
        HID h = HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h"));
        HID a = HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h/d/a"));

        assertThat(z.getRelativizedPath(x)).isEqualTo("y/x");
        assertThat(h.getRelativizedPath(a)).isEqualTo("d/a");

        assertThat(x.getRelativizedPath(x)).isNull(); // self
        assertThat(x.getRelativizedPath(z)).isNull(); // ancestor
        assertThat(z.getRelativizedPath(a)).isNull(); // container
    }

    @Test
    public void windowsParents() throws IOException {
        // HIDs take the URI approach in that the "root" appears like just another path segment
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.windows())) {
            Path path = fs.getPath("C:", "Program Files", "Internet Explorer");
            assume().that(Iterables.size(path)).isEqualTo(2); // The root component is separate!

            HID hid = HID.from(path);
            assertThat(hid.getName()).isEqualTo("Internet Explorer");
            assertThat(hid.getParent().getName()).isEqualTo("Program Files");
            assertThat(hid.getParent().getParent().getName()).isEqualTo("C:");
            assertThat(hid.getParent().getParent().isRoot()).isFalse();
            assertThat(hid.getParent().getParent().getParent().getName()).isEqualTo("/");
            assertThat(hid.getParent().getParent().getParent().isRoot()).isTrue();
        }
    }

    @Test
    public void fromHttpUrl() {
        // HIDs preserve the authority but don't otherwise expose it (much like the scheme)
        HID hid = HID.from(URI.create("http://example.com/foo/bar"));
        assertThat(hid.toString()).contains("example.com");
        assertThat(hid.toUri().getHost()).isEqualTo("example.com");
        while (hid != null) {
            assertThat(hid.getName()).doesNotContain("example.com");
            hid = hid.getParent();
        }
    }

    @Test
    public void pathNames() {
        assertThat(HID.from(URI.create("file:/x/y/z")).getPathNames()).containsExactly("x", "y", "z");
        assertThat(HID.from(URI.create("tar:file:%2Fz%2Fy%2Fx#/h/d/a")).getPathNames()).containsExactly("h", "d", "a");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutablePathNames() {
        HID.from(URI.create("file:/x/y/z")).getPathNames().add("a");
    }

    @Test
    public void schemeNesting() {
        final HID hid = HID.from(URI.create("tar:file:%2F%2F%2Ffoo%2Fbar.tar#/abc/def/ghi.txt"));
        assertThat(hid.getScheme()).isEqualTo("tar");
        assertThat(hid.getContainer().getScheme()).isEqualTo("file");
    }

}
