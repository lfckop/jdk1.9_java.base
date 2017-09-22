/*
 * Copyright (c) 2014, 2016, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.jimage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

/**
 * @implNote This class needs to maintain JDK 8 source compatibility.
 *
 * It is used internally in the JDK to implement jimage/jrtfs access,
 * but also compiled and delivered as part of the jrtfs.jar to support access
 * to the jimage file provided by the shipped JDK by tools running on JDK 8.
 */
public final class ImageHeader {
    public static final int MAGIC = 0xCAFEDADA;
    public static final int MAJOR_VERSION = 1;
    public static final int MINOR_VERSION = 0;
    private static final int HEADER_SLOTS = 7;

    private final int magic;
    private final int majorVersion;
    private final int minorVersion;
    private final int flags;
    private final int resourceCount;
    private final int tableLength;
    private final int locationsSize;
    private final int stringsSize;

    public ImageHeader(int resourceCount, int tableCount,
            int locationsSize, int stringsSize) {
        this(MAGIC, MAJOR_VERSION, MINOR_VERSION, 0, resourceCount,
                tableCount, locationsSize, stringsSize);
    }

    public ImageHeader(int magic, int majorVersion, int minorVersion,
                int flags, int resourceCount,
                int tableLength, int locationsSize, int stringsSize)
    {
        this.magic = magic;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.flags = flags;
        this.resourceCount = resourceCount;
        this.tableLength = tableLength;
        this.locationsSize = locationsSize;
        this.stringsSize = stringsSize;
    }

    public static int getHeaderSize() {
       return HEADER_SLOTS * 4;
    }

    static ImageHeader readFrom(IntBuffer buffer) {
        Objects.requireNonNull(buffer);

        if (buffer.capacity() != HEADER_SLOTS) {
            throw new InternalError(
                "jimage header not the correct size: " + buffer.capacity());
        }

        int magic = buffer.get(0);
        int version = buffer.get(1);
        int majorVersion = version >>> 16;
        int minorVersion = version & 0xFFFF;
        int flags = buffer.get(2);
        int resourceCount = buffer.get(3);
        int tableLength = buffer.get(4);
        int locationsSize = buffer.get(5);
        int stringsSize = buffer.get(6);

        return new ImageHeader(magic, majorVersion, minorVersion, flags,
            resourceCount, tableLength, locationsSize, stringsSize);
    }

    public void writeTo(ImageStream stream) {
        Objects.requireNonNull(stream);
        stream.ensure(getHeaderSize());
        writeTo(stream.getBuffer());
    }

    public void writeTo(ByteBuffer buffer) {
        Objects.requireNonNull(buffer);
        buffer.putInt(magic);
        buffer.putInt(majorVersion << 16 | minorVersion);
        buffer.putInt(flags);
        buffer.putInt(resourceCount);
        buffer.putInt(tableLength);
        buffer.putInt(locationsSize);
        buffer.putInt(stringsSize);
    }

    public int getMagic() {
        return magic;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getFlags() {
        return flags;
    }

    public int getResourceCount() {
        return resourceCount;
    }

    public int getTableLength() {
        return tableLength;
    }

    public int getRedirectSize() {
        return tableLength * 4;
    }

    public int getOffsetsSize() {
        return tableLength * 4;
    }

    public int getLocationsSize() {
        return locationsSize;
    }

    public int getStringsSize() {
        return stringsSize;
    }

    public int getIndexSize() {
        return getHeaderSize() +
               getRedirectSize() +
               getOffsetsSize() +
               getLocationsSize() +
               getStringsSize();
    }

    int getRedirectOffset() {
        return getHeaderSize();
    }

    int getOffsetsOffset() {
        return getRedirectOffset() +
               getRedirectSize();
    }

    int getLocationsOffset() {
        return getOffsetsOffset() +
               getOffsetsSize();
    }

    int getStringsOffset() {
        return getLocationsOffset() +
               getLocationsSize();
    }
}
