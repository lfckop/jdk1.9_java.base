/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package sun.nio.ch;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.*;


class SinkChannelImpl
    extends Pipe.SinkChannel
    implements SelChImpl
{

    // Used to make native read and write calls
    private static final NativeDispatcher nd = new FileDispatcherImpl();

    // The file descriptor associated with this channel
    FileDescriptor fd;

    // fd value needed for dev/poll. This value will remain valid
    // even after the value in the file descriptor object has been set to -1
    int fdVal;

    // ID of native thread doing write, for signalling
    private volatile long thread;

    // Lock held by current reading thread
    private final Object lock = new Object();

    // Lock held by any thread that modifies the state fields declared below
    // DO NOT invoke a blocking I/O operation while holding this lock!
    private final Object stateLock = new Object();

    // -- The following fields are protected by stateLock

    // Channel state
    private static final int ST_UNINITIALIZED = -1;
    private static final int ST_INUSE = 0;
    private static final int ST_KILLED = 1;
    private volatile int state = ST_UNINITIALIZED;

    // -- End of fields protected by stateLock


    public FileDescriptor getFD() {
        return fd;
    }

    public int getFDVal() {
        return fdVal;
    }

    SinkChannelImpl(SelectorProvider sp, FileDescriptor fd) {
        super(sp);
        this.fd = fd;
        this.fdVal = IOUtil.fdVal(fd);
        this.state = ST_INUSE;
    }

    protected void implCloseSelectableChannel() throws IOException {
        synchronized (stateLock) {
            if (state != ST_KILLED)
                nd.preClose(fd);
            long th = thread;
            if (th != 0)
                NativeThread.signal(th);
            if (!isRegistered())
                kill();
        }
    }

    public void kill() throws IOException {
        synchronized (stateLock) {
            if (state == ST_KILLED)
                return;
            if (state == ST_UNINITIALIZED) {
                state = ST_KILLED;
                return;
            }
            assert !isOpen() && !isRegistered();
            nd.close(fd);
            state = ST_KILLED;
        }
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        IOUtil.configureBlocking(fd, block);
    }

    public boolean translateReadyOps(int ops, int initialOps,
                                     SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps();// Do this just once, it synchronizes
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;

        if ((ops & Net.POLLNVAL) != 0)
            throw new Error("POLLNVAL detected");

        if ((ops & (Net.POLLERR | Net.POLLHUP)) != 0) {
            newOps = intOps;
            sk.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }

        if (((ops & Net.POLLOUT) != 0) &&
            ((intOps & SelectionKey.OP_WRITE) != 0))
            newOps |= SelectionKey.OP_WRITE;

        sk.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        if (ops == SelectionKey.OP_WRITE)
            ops = Net.POLLOUT;
        sk.selector.putEventOps(sk, ops);
    }

    private void ensureOpen() throws IOException {
        if (!isOpen())
            throw new ClosedChannelException();
    }

    public int write(ByteBuffer src) throws IOException {
        ensureOpen();
        synchronized (lock) {
            int n = 0;
            try {
                begin();
                if (!isOpen())
                    return 0;
                thread = NativeThread.current();
                do {
                    n = IOUtil.write(fd, src, -1, nd);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return IOStatus.normalize(n);
            } finally {
                thread = 0;
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                assert IOStatus.check(n);
            }
        }
    }

    public long write(ByteBuffer[] srcs) throws IOException {
        if (srcs == null)
            throw new NullPointerException();
        ensureOpen();
        synchronized (lock) {
            long n = 0;
            try {
                begin();
                if (!isOpen())
                    return 0;
                thread = NativeThread.current();
                do {
                    n = IOUtil.write(fd, srcs, nd);
                } while ((n == IOStatus.INTERRUPTED) && isOpen());
                return IOStatus.normalize(n);
            } finally {
                thread = 0;
                end((n > 0) || (n == IOStatus.UNAVAILABLE));
                assert IOStatus.check(n);
            }
        }
    }

    public long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException
    {
        if ((offset < 0) || (length < 0) || (offset > srcs.length - length))
           throw new IndexOutOfBoundsException();
        return write(Util.subsequence(srcs, offset, length));
    }
}
