/*
 * Copyright (c) 2008, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.nio.fs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileTypeDetector;
import sun.security.action.GetPropertyAction;

/**
 * MacOSX implementation of FileSystemProvider
 */

public class MacOSXFileSystemProvider extends BsdFileSystemProvider {
    public MacOSXFileSystemProvider() {
        super();
    }

    @Override
    MacOSXFileSystem newFileSystem(String dir) {
        return new MacOSXFileSystem(this, dir);
    }

    @Override
    FileTypeDetector getFileTypeDetector() {
        Path userMimeTypes = Paths.get(GetPropertyAction
                .privilegedGetProperty("user.home"), ".mime.types");

        return chain(new MimeTypesFileTypeDetector(userMimeTypes),
                     new UTIFileTypeDetector());
    }
}
