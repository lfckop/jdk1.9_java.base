/*
 * Copyright (c) 2008, 2012, Oracle and/or its affiliates. All rights reserved.
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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Bsd specific system calls.
 */

class BsdNativeDispatcher extends UnixNativeDispatcher {
    protected BsdNativeDispatcher() { }

   /**
    * struct fsstat_iter *getfsstat();
    */
    static native long getfsstat() throws UnixException;

   /**
    * int fsstatEntry(struct fsstat_iter * iter, UnixMountEntry entry);
    */
    static native int fsstatEntry(long iter, UnixMountEntry entry)
        throws UnixException;

   /**
    * void endfsstat(struct fsstat_iter * iter);
    */
    static native void endfsstat(long iter) throws UnixException;

    // initialize field IDs
    private static native void initIDs();

    static {
         initIDs();
    }
}
