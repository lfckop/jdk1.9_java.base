/*
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
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

import jdk.internal.misc.Unsafe;

/**
 * Factory for native buffers.
 */

class NativeBuffers {
    private NativeBuffers() { }

    private static final Unsafe unsafe = Unsafe.getUnsafe();

    private static final int TEMP_BUF_POOL_SIZE = 3;
    private static ThreadLocal<NativeBuffer[]> threadLocal =
        new ThreadLocal<NativeBuffer[]>();

    /**
     * Allocates a native buffer, of at least the given size, from the heap.
     */
    static NativeBuffer allocNativeBuffer(int size) {
        // Make a new one of at least 2k
        if (size < 2048) size = 2048;
        return new NativeBuffer(size);
    }

    /**
     * Returns a native buffer, of at least the given size, from the thread
     * local cache.
     */
    static NativeBuffer getNativeBufferFromCache(int size) {
        // return from cache if possible
        NativeBuffer[] buffers = threadLocal.get();
        if (buffers != null) {
            for (int i=0; i<TEMP_BUF_POOL_SIZE; i++) {
                NativeBuffer buffer = buffers[i];
                if (buffer != null && buffer.size() >= size) {
                    buffers[i] = null;
                    return buffer;
                }
            }
        }
        return null;
    }

    /**
     * Returns a native buffer, of at least the given size. The native buffer
     * is taken from the thread local cache if possible; otherwise it is
     * allocated from the heap.
     */
    static NativeBuffer getNativeBuffer(int size) {
        NativeBuffer buffer = getNativeBufferFromCache(size);
        if (buffer != null) {
            buffer.setOwner(null);
            return buffer;
        } else {
            return allocNativeBuffer(size);
        }
    }

    /**
     * Releases the given buffer. If there is space in the thread local cache
     * then the buffer goes into the cache; otherwise the memory is deallocated.
     */
    static void releaseNativeBuffer(NativeBuffer buffer) {
        // create cache if it doesn't exist
        NativeBuffer[] buffers = threadLocal.get();
        if (buffers == null) {
            buffers = new NativeBuffer[TEMP_BUF_POOL_SIZE];
            buffers[0] = buffer;
            threadLocal.set(buffers);
            return;
        }
        // Put it in an empty slot if such exists
        for (int i=0; i<TEMP_BUF_POOL_SIZE; i++) {
            if (buffers[i] == null) {
                buffers[i] = buffer;
                return;
            }
        }
        // Otherwise replace a smaller one in the cache if such exists
        for (int i=0; i<TEMP_BUF_POOL_SIZE; i++) {
            NativeBuffer existing = buffers[i];
            if (existing.size() < buffer.size()) {
                existing.free();
                buffers[i] = buffer;
                return;
            }
        }

        // free it
        buffer.free();
    }

    /**
     * Copies a byte array and zero terminator into a given native buffer.
     */
    static void copyCStringToNativeBuffer(byte[] cstr, NativeBuffer buffer) {
        long offset = Unsafe.ARRAY_BYTE_BASE_OFFSET;
        long len = cstr.length;
        assert buffer.size() >= (len + 1);
        unsafe.copyMemory(cstr, offset, null, buffer.address(), len);
        unsafe.putByte(buffer.address() + len, (byte)0);
    }

    /**
     * Copies a byte array and zero terminator into a native buffer, returning
     * the buffer.
     */
    static NativeBuffer asNativeBuffer(byte[] cstr) {
        NativeBuffer buffer = getNativeBuffer(cstr.length+1);
        copyCStringToNativeBuffer(cstr, buffer);
        return buffer;
    }
}
