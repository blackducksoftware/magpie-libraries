/*
 * Copyright 2017 Black Duck Software, Inc.
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

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@code Product}.
 *
 * @author jgustie
 */
public class ProductTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void productNameOnly() {
        Product product = Product.parse("test");
        assertThat(product.name()).isEqualTo("test");
        assertThat(product.version()).isNull();
        assertThat(product.comments()).isEmpty();
    }

    @Test
    public void productNameAndVersion() {
        Product product = Product.parse("test/1");
        assertThat(product.name()).isEqualTo("test");
        assertThat(product.version()).isEqualTo("1");
        assertThat(product.comments()).isEmpty();
    }

    @Test
    public void productNameAndVersionAndComment() {
        Product product = Product.parse("test/1 (foobar)");
        assertThat(product.name()).isEqualTo("test");
        assertThat(product.version()).isEqualTo("1");
        assertThat(product.comments()).containsExactly("(foobar)");
    }

    @Test
    public void missingRequiredWhitespace() {
        thrown.expect(IllegalArgumentException.class);
        Product.parse("test/1(foobar)");
    }

    @Test
    public void multipleComments() {
        assertThat(Product.parse("foo/1.0 (bar)(gus)").comments()).containsExactly("(bar)", "(gus)");
        assertThat(Product.parse("foo/1.0 (bar) (gus)").comments()).containsExactly("(bar)", "(gus)");
        assertThat(Product.parse("foo/1.0 (bar)  (gus)").comments()).containsExactly("(bar)", "(gus)");

        assertThat(Product.parse("foo/1.0 (bar)(gus)(toad)").comments()).containsExactly("(bar)", "(gus)", "(toad)");
        assertThat(Product.parse("foo/1.0 (bar) (gus) (toad)").comments()).containsExactly("(bar)", "(gus)", "(toad)");
        assertThat(Product.parse("foo/1.0 (bar)  (gus)  (toad)").comments()).containsExactly("(bar)", "(gus)", "(toad)");
    }

    @Test
    public void commentsWithSpaces() {
        assertThat(Product.parse("foo/1.0 (bar gus)").comments()).contains("(bar gus)");
        assertThat(Product.parse("foo/1.0 (bar  gus)").comments()).contains("(bar  gus)");
    }

    @Test
    public void multipleNamesParseSingle() {
        thrown.expect(IllegalArgumentException.class);
        Product.parse("foo bar");
    }

    @Test
    public void multipleNamesVersionsParseSingle() {
        thrown.expect(IllegalArgumentException.class);
        Product.parse("foo/1.0 bar/1.0");
    }

    @Test
    public void multipleNamesNoVersionsNoComments() {
        List<Product> fooBar = Product.parseAll("foo bar");
        assertThat(fooBar).hasSize(2);
        assertThat(fooBar).containsExactly(Product.parse("foo"), Product.parse("bar")).inOrder();
    }

    @Test
    public void multipleNamesVersionsNoComments() {
        List<Product> fooBar = Product.parseAll("foo/1.0 bar/1.0");
        assertThat(fooBar).hasSize(2);
        assertThat(fooBar).containsExactly(Product.parse("foo/1.0"), Product.parse("bar/1.0")).inOrder();
    }

    @Test
    public void builderKeepsFirstDistinct() {
        assertThat(new Product.ListBuilder()
                .addProduct(Product.parse("foo"))
                .addProduct(Product.parse("bar"))
                .addProduct(Product.parse("foo"))
                .addProduct(Product.parse("gus"))
                .build().toString()).isEqualTo("foo bar gus");
    }

    @Test
    public void builderKeepsFirstIgnoresComments() {
        // TODO It would be nice if this produced "foo/1 (foo) (bar) bar gus"
        assertThat(new Product.ListBuilder()
                .addProduct(new Product.Builder().name("foo").version("1").addComment("(foo)").build())
                .addProduct(new Product.Builder().name("bar").build())
                .addProduct(new Product.Builder().name("foo").version("1").addComment("(bar)").build())
                .addProduct(new Product.Builder().name("gus").version("2").build())
                .build().toString()).isEqualTo("foo/1 (foo) bar gus/2");
    }

}
