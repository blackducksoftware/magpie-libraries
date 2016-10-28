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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * An alternate resource bundle control which has better control over character encoding.
 *
 * @author jgustie
 */
public class BundleControl extends ResourceBundle.Control {

    protected BundleControl() {}

    /**
     * Creates a new resource bundle control.
     */
    public static BundleControl create() {
        return new BundleControl();
    }

    /**
     * Creates a new resource bundle control for use in development. The resulting bundle
     * <em>does not cache</em> and may introduce performance issues when used excessively.
     */
    public static BundleControl createDevelopmentControl() {
        return new BundleControl() {
            @Override
            public long getTimeToLive(String baseName, Locale locale) {
                checkNotNull(baseName);
                checkNotNull(locale);
                return TTL_DONT_CACHE;
            }
        };
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        ResourceBundle result = null;
        if (FORMAT_PROPERTIES.contains(format)) {
            // Intercept attempts to load PropertyResourceBundle instances
            URL url = loader.getResource(toResourceName(toBundleName(baseName, locale), "properties"));
            if (url != null) {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(!reload);

                // It is extremely unlikely that the connection will have any type of encoding
                String encoding = firstNonNull(connection.getContentEncoding(), defaultCharset().name());
                try (Reader reader = new InputStreamReader(connection.getInputStream(), encoding)) {
                    result = new PropertyResourceBundle(reader);
                }
            }
        } else {
            // If we are not loading a PropertyResourceBundle, just let the super handle it
            result = super.newBundle(baseName, locale, format, loader, reload);
        }
        return result;
    }

}
