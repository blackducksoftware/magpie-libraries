/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.common.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;

/**
 * A bunch of stuff I was too lazy to find in Jackson, but assume must be in there somewhere.
 *
 * @author jgustie
 */
class JsonUtil {

    private static final Set<Class<?>> INTEGRAL_NUMBER_TYPES = ImmutableSet.of(
            Byte.class, byte.class, Short.class, short.class, Integer.class, int.class, Long.class, long.class, BigInteger.class);

    private static final Set<Class<?>> FLOATING_POINT_NUMBER_TYPES = ImmutableSet.of(
            Float.class, float.class, Double.class, double.class, BigDecimal.class);

    private static final Set<Class<?>> TEXTUAL_TYPES = ImmutableSet.of(String.class);

    private static final Set<Class<?>> BOOLEAN_TYPES = ImmutableSet.of(Boolean.class, boolean.class);

    /**
     * Checks if a node is compatible with a specific Java type.
     */
    static boolean isInstanceOfType(JsonNode node, Class<?> clazz) {
        return (node.isIntegralNumber() && INTEGRAL_NUMBER_TYPES.contains(clazz))
                || (node.isFloatingPointNumber() && FLOATING_POINT_NUMBER_TYPES.contains(clazz))
                || (node.isTextual() && TEXTUAL_TYPES.contains(clazz))
                || (node.isBoolean() && BOOLEAN_TYPES.contains(clazz));
    }

    /**
     * Returns a more detailed type name then the node type would provide.
     */
    static String typeName(JsonNode node) {
        if (node.isIntegralNumber()) {
            return "integral number";
        } else if (node.isFloatingPointNumber()) {
            return "floating point number";
        } else if (node.isTextual()) {
            return "textual";
        } else if (node.isBoolean()) {
            return "boolean";
        } else {
            return node.getClass().getName();
        }
    }

    /**
     * Unwraps a JSON node as an object.
     */
    @Nullable
    static Object unwrap(@Nullable JsonNode node) {
        if (node == null) {
            return null;
        } else if (node.isArray()) {
            return StreamSupport.stream(node.spliterator(), false)
                    .map(JsonUtil::unwrap).collect(Collectors.toList());
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else if (node.isDouble()) {
            return node.doubleValue();
        } else if (node.isFloat()) {
            return node.floatValue();
        } else if (node.isShort()) {
            return node.shortValue();
        } else if (node.isInt()) {
            return node.intValue();
        } else if (node.isLong()) {
            return node.longValue();
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isNull()) {
            return null;
        } else {
            return node;
        }
    }

    private JsonUtil() {
        assert false;
    }
}
