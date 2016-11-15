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
package com.blackducksoftware.common.test;

import static com.blackducksoftware.common.test.PathSubject.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

/**
 * Dummy test for the path subject to make sure we can compile.
 *
 * @author jgustie
 */
public class PathSubjectTest {

    @Test
    public void pathCompilesUnambigiously() {
        assertThat(Paths.get("/usr", "local")).isNotNull();
    }

}
