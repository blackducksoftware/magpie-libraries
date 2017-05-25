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
package com.blackducksoftware.common.nio.file.attribute;

import java.nio.file.attribute.PosixFileAttributes;

/**
 * File attributes for ARJ archive entries.
 *
 * @author jgustie
 */
// TODO Shouldn't this also have DosFileAttributes because ARJ was originally Windows specific?
public interface ArjArchiveEntryFileAttributes extends ArchiveEntryFileAttributes, PosixFileAttributes {

}