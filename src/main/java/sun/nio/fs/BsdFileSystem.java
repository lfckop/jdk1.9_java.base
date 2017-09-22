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

import java.nio.file.*;
import java.io.IOException;
import java.util.*;
import java.security.AccessController;
import sun.security.action.GetPropertyAction;

/**
 * Bsd implementation of FileSystem
 */

class BsdFileSystem extends UnixFileSystem {

    BsdFileSystem(UnixFileSystemProvider provider, String dir) {
        super(provider, dir);
    }

    @Override
    public WatchService newWatchService()
        throws IOException
    {
        // use polling implementation until we implement a BSD/kqueue one
        return new PollingWatchService();
    }

    // lazy initialization of the list of supported attribute views
    private static class SupportedFileFileAttributeViewsHolder {
        static final Set<String> supportedFileAttributeViews =
            supportedFileAttributeViews();
        private static Set<String> supportedFileAttributeViews() {
            Set<String> result = new HashSet<String>();
            result.addAll(standardFileAttributeViews());
            return Collections.unmodifiableSet(result);
        }
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return SupportedFileFileAttributeViewsHolder.supportedFileAttributeViews;
    }

    @Override
    void copyNonPosixAttributes(int ofd, int nfd) {
    }

    /**
     * Returns object to iterate over mount entries
     */
    @Override
    Iterable<UnixMountEntry> getMountEntries() {
        ArrayList<UnixMountEntry> entries = new ArrayList<UnixMountEntry>();
        try {
            long iter = BsdNativeDispatcher.getfsstat();
            try {
                for (;;) {
                    UnixMountEntry entry = new UnixMountEntry();
                    int res = BsdNativeDispatcher.fsstatEntry(iter, entry);
                    if (res < 0)
                        break;
                    entries.add(entry);
                }
            } finally {
                BsdNativeDispatcher.endfsstat(iter);
            }

        } catch (UnixException x) {
            // nothing we can do
        }
        return entries;
    }



    @Override
    FileStore getFileStore(UnixMountEntry entry) throws IOException {
        return new BsdFileStore(this, entry);
    }
}
