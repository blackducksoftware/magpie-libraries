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
import static com.google.common.truth.Truth8.assertThat;

import java.util.stream.Stream;

import org.junit.Test;

/**
 * Tests for {@code ProductList}.
 *
 * @author jgustie
 */
public class ProductListTest {

    @Test(expected = IllegalArgumentException.class)
    public void parse_empty() {
        ProductList.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void build_empty() {
        new ProductList.Builder().build();
    }

    @Test
    public void multipleNamesNoVersionsNoComments() {
        ProductList fooBar = ProductList.parse("foo bar");
        assertThat(fooBar).hasSize(2);
        assertThat(fooBar).containsExactly(Product.parse("foo"), Product.parse("bar")).inOrder();
    }

    @Test
    public void multipleNamesVersionsNoComments() {
        ProductList fooBar = ProductList.parse("foo/1.0 bar/1.0");
        assertThat(fooBar).hasSize(2);
        assertThat(fooBar).containsExactly(Product.parse("foo/1.0"), Product.parse("bar/1.0")).inOrder();
    }

    @Test
    public void multipleNamesVersionsComments() {
        ProductList fooBar = ProductList.parse("foo/1.0 (test1) bar/1.0");
        assertThat(fooBar).hasSize(2);
        assertThat(fooBar).containsExactly(Product.parse("foo/1.0 (test1)"), Product.parse("bar/1.0")).inOrder();
    }

    @Test
    public void primary() {
        ProductList fooBar = ProductList.parse("foo bar");
        assertThat(fooBar.primary()).isEqualTo(Product.parse("foo"));
    }

    @Test
    public void findByName_present() {
        ProductList fooBar = ProductList.parse("foo bar");
        assertThat(fooBar.tryFind(p -> p.name().equals("bar"))).hasValue(Product.parse("bar"));
    }

    @Test
    public void find_empty() {
        assertThat(ProductList.parse("foo bar").tryFind(x -> false)).isEmpty();
    }

    @Test
    public void builderKeepsFirstDistinct() {
        assertThat(new ProductList.Builder()
                .addProduct(Product.parse("foo"))
                .addProduct(Product.parse("bar"))
                .addProduct(Product.parse("foo"))
                .addProduct(Product.parse("gus"))
                .build().toString()).isEqualTo("foo bar gus");
    }

    @Test
    public void builderKeepsFirstIgnoresComments() {
        assertThat(new ProductList.Builder()
                .addProduct(new Product.Builder().name("foo").version("1").addComment("(foo)").build())
                .addProduct(new Product.Builder().name("bar").build())
                .addProduct(new Product.Builder().name("foo").version("1").addComment("(bar)").build())
                .addProduct(new Product.Builder().name("gus").version("2").build())
                .build().toString()).isEqualTo("foo/1 (foo) bar gus/2");
    }

    @Test
    public void builderMergeProduct() {
        assertThat(new ProductList.Builder()
                .addProduct(new Product.Builder().name("foo").addComment("(foo)").build())
                .mergeProduct(new Product.Builder().name("foo").version("1").addComment("(bar)").build())
                .addProduct(new Product.Builder().name("gus").version("2").build())
                .mergeProduct(new Product.Builder().name("gus").version("3").build())
                .build().toString()).isEqualTo("foo/1 (foo) (bar) gus/2");
    }

    @Test
    public void builderMergeProductEmpty() {
        assertThat(new ProductList.Builder()
                .mergeProduct(new Product.Builder().name("foo").version("1").addComment("(bar)").build())
                .build().toString()).isEqualTo("foo/1 (bar)");
    }

    @Test
    public void builderMergeProductDuplicateComments() {
        assertThat(new ProductList.Builder()
                .addProduct(new Product.Builder().name("foo").addComment("(foo)").build())
                .mergeProduct(new Product.Builder().name("foo").addComment("(foo)").build())
                .build().toString()).isEqualTo("foo (foo)");
    }

    @Test
    public void streamProducts() {
        assertThat(ProductList.parse("foo bar gus").stream())
                .containsExactly(Product.parse("foo"), Product.parse("bar"), Product.parse("gus"))
                .inOrder();
    }

    @Test
    public void collectProducts() {
        assertThat(Stream.of(Product.parse("foo"), Product.parse("bar"), Product.parse("gus")).collect(ProductList.toProductList()).toString())
                .isEqualTo("foo bar gus");
    }

}
