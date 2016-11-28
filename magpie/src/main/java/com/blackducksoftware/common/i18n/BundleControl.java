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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.common.annotations.VisibleForTesting;

/**
 * An alternate resource bundle control which has better control over character encoding. And by "better control" we
 * mean you get UTF-8.
 *
 * @author jgustie
 */
public class BundleControl extends ResourceBundle.Control {

    /**
     * The encoding to use when the loaded resource does not expose one through URL headers.
     */
    private final Charset defaultEncoding;

    @VisibleForTesting
    protected BundleControl(Charset defaultEncoding) {
        this.defaultEncoding = Objects.requireNonNull(defaultEncoding);
    }

    /**
     * Creates a new resource bundle control for loading UTF-8 encoded property bundles.
     */
    public static BundleControl create() {
        return new BundleControl(StandardCharsets.UTF_8);
    }

    /**
     * Creates a new resource bundle control for use in development. The resulting bundle
     * <em>does not cache</em> and may introduce performance issues when used excessively.
     */
    public static BundleControl createDevelopmentControl() {
        return new BundleControl(StandardCharsets.UTF_8) {
            @Override
            public long getTimeToLive(String baseName, Locale locale) {
                Objects.requireNonNull(baseName);
                Objects.requireNonNull(locale);
                return TTL_DONT_CACHE;
            }
        };
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        ResourceBundle result = null;
        if (FORMAT_PROPERTIES.contains(format)) {
            URL url = loader.getResource(toResourceName(toBundleName(baseName, locale), "properties"));
            if (url != null) {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(!reload);

                // It is extremely unlikely that the connection will have any type of encoding
                Charset encoding = Optional.ofNullable(connection.getContentEncoding())
                        .map(Charset::forName)
                        .orElse(defaultEncoding);

                try (Reader reader = new InputStreamReader(connection.getInputStream(), encoding)) {
                    result = new PropertyResourceBundle(reader);
                }
            }
        } else {
            result = super.newBundle(baseName, locale, format, loader, reload);
        }
        return result;
    }

}
