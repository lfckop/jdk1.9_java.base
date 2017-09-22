/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * File type detector that uses a file extension to look up its MIME type
 * via the Apple Uniform Type Identifier interfaces.
 */
class UTIFileTypeDetector extends AbstractFileTypeDetector {
    UTIFileTypeDetector() {
        super();
    }

    private native String probe0(String fileExtension) throws IOException;

    @Override
    protected String implProbeContentType(Path path) throws IOException {
        Path fn = path.getFileName();
        if (fn == null)
            return null;  // no file name

        String ext = getExtension(fn.toString());
        if (ext.isEmpty())
            return null;  // no extension

        return probe0(ext);
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<>() {
            @Override
            public Void run() {
                System.loadLibrary("nio");
                return null;
            }
        });
    }
}
