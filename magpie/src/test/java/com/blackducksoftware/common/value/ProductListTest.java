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

import org.junit.Test;

/**
 * Tests for {@code ProductList}.
 *
 * @author jgustie
 */
public class ProductListTest {

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
        // TODO It would be nice if this produced "foo/1 (foo) (bar) bar gus"
        assertThat(new ProductList.Builder()
                .addProduct(new Product.Builder().name("foo").version("1").addComment("(foo)").build())
                .addProduct(new Product.Builder().name("bar").build())
                .addProduct(new Product.Builder().name("foo").version("1").addComment("(bar)").build())
                .addProduct(new Product.Builder().name("gus").version("2").build())
                .build().toString()).isEqualTo("foo/1 (foo) bar gus/2");
    }

}
