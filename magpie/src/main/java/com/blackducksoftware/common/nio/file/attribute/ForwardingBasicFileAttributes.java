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

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

/**
 * A forwarding implementation of basic file attributes. Useful when implementing basic file attributes when the core
 * attributes should come from another instance of basic file attributes.
 *
 * @author jgustie
 */
public abstract class ForwardingBasicFileAttributes implements BasicFileAttributes {

    /**
     * A simple wrapper of a basic file attributes.
     */
    public static class SimpleBasicFileAttributes extends ForwardingBasicFileAttributes {

        private final BasicFileAttributes delegate;

        protected SimpleBasicFileAttributes(BasicFileAttributes delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        protected final BasicFileAttributes delegate() {
            return delegate;
        }
    }

    protected ForwardingBasicFileAttributes() {
    }

    protected abstract BasicFileAttributes delegate();

    @Override
    public FileTime lastModifiedTime() {
        return delegate().lastModifiedTime();
    }

    @Override
    public FileTime lastAccessTime() {
        return delegate().lastAccessTime();
    }

    @Override
    public FileTime creationTime() {
        return delegate().creationTime();
    }

    @Override
    public boolean isRegularFile() {
        return delegate().isRegularFile();
    }

    @Override
    public boolean isDirectory() {
        return delegate().isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
        return delegate().isSymbolicLink();
    }

    @Override
    public boolean isOther() {
        return delegate().isOther();
    }

    @Override
    public long size() {
        return delegate().size();
    }

    @Override
    public Object fileKey() {
        return delegate().fileKey();
    }

}
