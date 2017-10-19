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

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Helper for creating in-memory file systems with contents.
 *
 * @author jgustie
 */
public class FileSystemBuilder {

    /**
     * The Jimfs configuration to use when creating the in-memory file system.
     */
    private Configuration.Builder config;

    /**
     * A list of directory paths to create.
     */
    private final List<String> directories = new ArrayList<>();

    /**
     * A map of file paths to contents (expressed as "lines").
     */
    private final ListMultimap<String, String> fileContents = LinkedListMultimap.create();

    public FileSystemBuilder() {
        config = Configuration.unix().toBuilder();
    }

    /**
     * Replace the file system configuration with one that mimics Windows.
     */
    public FileSystemBuilder asWindows() {
        config = Configuration.windows().toBuilder();
        return this;
    }

    /**
     * Adds a (potentially empty) directory to the resulting file system.
     */
    public FileSystemBuilder addDirectory(String path) {
        checkArgument(path.startsWith("/"), "path must start with /");
        directories.add(path);
        return this;
    }

    /**
     * Adds a file with the specified contents to the resulting file system. All missing parent directories will be
     * created to ensure the file can be written without error (i.e. there is no need to explicitly add the
     * directories specified in your path).
     */
    public FileSystemBuilder addFile(String path, String... lines) {
        checkArgument(path.startsWith("/"), "path must start with /");
        fileContents.putAll(path, Arrays.asList(lines));
        return this;
    }

    /**
     * Adds a file with the contents "Hello World!" to the resulting file system.
     */
    public FileSystemBuilder addHelloWorld(String path) {
        return addFile(path, "Hello World!");
    }

    /**
     * Creates the in-memory file system as specified.
     */
    public FileSystem build() throws IOException {
        FileSystem fileSystem = Jimfs.newFileSystem(config.build());
        createContent(Iterables.getOnlyElement(fileSystem.getRootDirectories()));
        return fileSystem;
    }

    /**
     * Creates the file contents relative to the specified root directory.
     * <p>
     * This is useful if you want to write out the file system for comparing to {@code git ls-files}.
     */
    public void createContent(Path root) throws IOException {
        for (String dir : directories) {
            Files.createDirectories(root.resolve(dir.substring(1).replace("/", root.getFileSystem().getSeparator())));
        }
        for (Entry<String, Collection<String>> file : fileContents.asMap().entrySet()) {
            Path filePath = root.resolve(file.getKey().substring(1).replace("/", root.getFileSystem().getSeparator()));
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getValue(), UTF_8);
        }
    }
}
