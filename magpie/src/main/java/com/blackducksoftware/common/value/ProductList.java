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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * A list of one or more {@code Product} instances.
 *
 * @author jgustie
 */
public class ProductList implements Iterable<Product> {

    private final ImmutableList<Product> products;

    private ProductList(Builder builder) {
        checkArgument(!builder.products.isEmpty(), "product list requires at least one product identifier");
        products = builder.products.stream().distinct().collect(toImmutableList());
    }

    public Product primary() {
        return products.get(0);
    }

    public Optional<Product> tryFind(Predicate<Product> predicate) {
        for (Product product : products) {
            if (predicate.test(product)) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterator<Product> iterator() {
        return products.iterator();
    }

    @Override
    public int hashCode() {
        return Objects.hash(products);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProductList) {
            ProductList other = (ProductList) obj;
            return products.equals(other.products);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return products.stream().map(Product::toString).collect(Collectors.joining(" "));
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static ProductList parse(CharSequence input) {
        Builder builder = new Builder();
        builder.parse(input);
        return builder.build();
    }

    public static ProductList of(Product product) {
        return new Builder().addProduct(product).build();
    }

    public static ProductList from(Object obj) {
        if (obj instanceof ProductList) {
            return (ProductList) obj;
        } else if (obj instanceof Product) {
            return of((Product) obj);
        } else if (obj instanceof CharSequence) {
            return parse((CharSequence) obj);
        } else {
            throw new IllegalArgumentException("unexpected input: " + obj);
        }
    }

    public static class Builder {

        private List<Product> products;

        public Builder() {
            products = new ArrayList<>();
        }

        private Builder(ProductList productList) {
            products = new ArrayList<>(productList.products);
        }

        public Builder addProduct(Product product) {
            products.add(Objects.requireNonNull(product));
            return this;
        }

        public Builder mergeProduct(Product product) {
            Objects.requireNonNull(product);
            ListIterator<Product> i = products.listIterator();
            while (i.hasNext()) {
                Product existing = i.next();
                if (existing.name().equals(product.name())) {
                    Product.Builder builder = existing.newBuilder();
                    if (existing.version() == null) {
                        builder.version(product.version());
                    }
                    for (String comment : product.comments()) {
                        if (!existing.comments().contains(comment)) {
                            builder.addComment(comment);
                        }
                    }
                    i.set(builder.build());
                    return this;
                }
            }
            i.add(product);
            return this;
        }

        public ProductList build() {
            return new ProductList(this);
        }

        void parse(CharSequence input) {
            Product.Builder builder = new Product.Builder();
            List<CharSequence> tokens = new ArrayList<>();
            Rules.remainingTokens(input, 0, tokens::add);
            for (int i = 0; i < tokens.size();) {
                builder.parse(tokens.get(i));
                while (++i < tokens.size() && Rules.matchesWithQuotes(tokens.get(i), '(', ')', x -> true)) {
                    builder.addComment(tokens.get(i));
                }
                addProduct(builder.build());
            }
        }

    }

}
