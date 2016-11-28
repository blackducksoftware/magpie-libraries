/*
 * Copyright 2014 Black Duck Software, Inc.
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
package com.blackducksoftware.common.i18n;

import static com.google.common.truth.Truth.assertThat;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the {@code BundleControl} encodings.
 * <p>
 * There are a few properties files that are required for this test (currently they are checked in,
 * but we could create them and load them via. a separate class loader if desired). These properties
 * files contain all of the characters from the data provider and are explicitly encoded using
 * character set they are named after (e.g. "UTF-8.properties" is "UTF-8" encoded). If a character
 * set does not support a particular character, the Unicode escape sequence must be used (the
 * {@code Properties} class handles these escapes for us); see ISO-8859-1 as an example.
 *
 * @author jgustie
 */
@RunWith(Parameterized.class)
public class BundleControlEncodingTest {

    /**
     * This is a very small subset of characters that are often encoded differently.
     */
    @Parameters
    public static List<Object[]> charsetData() {
        return Arrays.asList(new Object[][] {
                { "COPYRIGHT_SIGN", "\u00a9" },
                { "LEFT_DOUBLE_QUOTATION_MARK", "\u201c" },
                { "DAGGER", "\u2020" },
                { "CENT_SIGN", "\u00a2" },
                { "TILDE", "\u007e" }, });
    }

    /**
     * Loads the specified bundle from the {@linkplain Locale#ROOT root locale} using the
     * {@code BundleControl} and the class loader of the test class.
     */
    protected static ResourceBundle getEncodedBundle(String charsetName) {
        return ResourceBundle.getBundle(BundleControlEncodingTest.class.getPackage().getName() + '.' + charsetName,
                Locale.ROOT,
                BundleControlEncodingTest.class.getClassLoader(),
                new BundleControl(Charset.forName(charsetName)));
    }

    // =============================================================================================

    private String key, value;

    public BundleControlEncodingTest(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Test
    public void testUtf8Encoding() {
        assertThat(getEncodedBundle("UTF-8").getString(key)).isEqualTo(value);
    }

    @Test
    public void testIso88591Encoding() {
        assertThat(getEncodedBundle("ISO-8859-1").getString(key)).isEqualTo(value);
    }

    @Test
    public void testWindows1252Encoding() {
        assertThat(getEncodedBundle("windows-1252").getString(key)).isEqualTo(value);
    }

}
