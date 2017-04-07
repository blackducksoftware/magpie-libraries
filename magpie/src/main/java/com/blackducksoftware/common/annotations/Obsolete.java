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
package com.blackducksoftware.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An element annotated as &#64;Obsolete is "softly deprecated". A suitable replacement should be identified which may
 * or may not be available in all contexts. A common usage is handling "polyfill" classes or methods which are providing
 * features that have been added in a future version of the Java (or other library) API.
 * <p>
 * Tools are encouraged to test the {@code see} value in effort to identify a suitable replacement; if such a
 * replacement is not available in the current context (compilation) usage of obsolete methods should not produce a
 * warning. If the {@code see} value can be resolved to a suitable and available replacement in the current context,
 * tools should produce a warning.
 *
 * @author jgustie
 */
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.METHOD,
        ElementType.TYPE,
})
@Documented
public @interface Obsolete {

    /**
     * Arbitrary textual notes related to the obsolete status of the annotated element. Useful for documentation.
     */
    String value() default "";

    /**
     * Identifies a potential replacement or a pointer to information related to the replacement of the annotated
     * element. The value should follow the format expected by the Javadoc {@code &#64;see} tag: e.g. a fully qualified
     * class with an optional method (separated by a "#") or an HTML anchor tag with link to more detailed information.
     */
    String see() default "";

}
