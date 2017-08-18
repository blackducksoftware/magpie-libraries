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

}
