/*
 * Copyright 2016 Black Duck Software, Inc.
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
package com.blackducksoftware.common.security;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Resources;

/**
 * Base class for tests in the security package.
 *
 * @author jgustie
 */
public abstract class AbstractSecurityTest {

    public final Path file(String name) {
        try {
            return Paths.get(Resources.getResource(getClass(), name).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid uri: " + name, e);
        }
    }

}
