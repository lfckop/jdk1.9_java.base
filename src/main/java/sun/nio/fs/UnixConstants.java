/*
 * Copyright (c) 2008, 2016, Oracle and/or its affiliates. All rights reserved.
 *
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
 *
 */

// AUTOMATICALLY GENERATED FILE - DO NOT EDIT

package sun.nio.fs;
class UnixConstants {
    private UnixConstants() { }
    static final int O_RDONLY = 0x0000;
    static final int O_WRONLY = 0x0001;
    static final int O_RDWR = 0x0002;
    static final int O_APPEND = 0x0008;
    static final int O_CREAT = 0x0200;
    static final int O_EXCL = 0x0800;
    static final int O_TRUNC = 0x0400;
    static final int O_SYNC = 0x0080;





    static final int O_DSYNC = 0x400000;



    static final int O_NOFOLLOW = 0x0100;






    static final int S_IAMB =
        (0000400|0000200|0000100|0000040|0000020|0000010|0000004|0000002|0000001);
    static final int S_IRUSR = 0000400;
    static final int S_IWUSR = 0000200;
    static final int S_IXUSR = 0000100;
    static final int S_IRGRP = 0000040;
    static final int S_IWGRP = 0000020;
    static final int S_IXGRP = 0000010;
    static final int S_IROTH = 0000004;
    static final int S_IWOTH = 0000002;
    static final int S_IXOTH = 0000001;

    static final int S_IFMT = 0170000;
    static final int S_IFREG = 0100000;
    static final int S_IFDIR = 0040000;
    static final int S_IFLNK = 0120000;
    static final int S_IFCHR = 0020000;
    static final int S_IFBLK = 0060000;
    static final int S_IFIFO = 0010000;
    static final int R_OK = (1<<2);
    static final int W_OK = (1<<1);
    static final int X_OK = (1<<0);
    static final int F_OK = 0;
    static final int ENOENT = 2;
    static final int ENXIO = 6;
    static final int EACCES = 13;
    static final int EEXIST = 17;
    static final int ENOTDIR = 20;
    static final int EINVAL = 22;
    static final int EXDEV = 18;
    static final int EISDIR = 21;
    static final int ENOTEMPTY = 66;
    static final int ENOSPC = 28;
    static final int EAGAIN = 35;
    static final int ENOSYS = 78;
    static final int ELOOP = 62;
    static final int EROFS = 30;





    static final int ENODATA = 96;


    static final int ERANGE = 34;
    static final int EMFILE = 24;







    static final int AT_SYMLINK_NOFOLLOW = 00;
    static final int AT_REMOVEDIR = 00;


}
