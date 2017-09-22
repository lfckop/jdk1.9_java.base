/*
 * Copyright (c) 2007, 2016, Oracle and/or its affiliates. All rights reserved.
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
package java.net;

import java.io.IOException;
import java.io.FileDescriptor;
import java.util.Set;
import java.util.HashSet;
import sun.net.ext.ExtendedSocketOptions;

/*
 * On Unix systems we simply delegate to native methods.
 *
 * @author Chris Hegarty
 */

class PlainSocketImpl extends AbstractPlainSocketImpl
{
    static {
        initProto();
    }

    /**
     * Constructs an empty instance.
     */
    PlainSocketImpl() { }

    /**
     * Constructs an instance with the given file descriptor.
     */
    PlainSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }

    static final ExtendedSocketOptions extendedOptions =
            ExtendedSocketOptions.getInstance();

    protected <T> void setOption(SocketOption<T> name, T value) throws IOException {
        if (!extendedOptions.isOptionSupported(name)) {
            if (!name.equals(StandardSocketOptions.SO_REUSEPORT)) {
                super.setOption(name, value);
            } else {
                if (supportedOptions().contains(name)) {
                    super.setOption(name, value);
                } else {
                    throw new UnsupportedOperationException("unsupported option");
                }
            }
        } else {
            if (getSocket() == null) {
                throw new UnsupportedOperationException("unsupported option");
            }
            if (isClosedOrPending()) {
                throw new SocketException("Socket closed");
            }
            extendedOptions.setOption(fd, name, value);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T getOption(SocketOption<T> name) throws IOException {
        if (!extendedOptions.isOptionSupported(name)) {
            if (!name.equals(StandardSocketOptions.SO_REUSEPORT)) {
                return super.getOption(name);
            } else {
                if (supportedOptions().contains(name)) {
                    return super.getOption(name);
                } else {
                    throw new UnsupportedOperationException("unsupported option");
                }
            }
        } else {
            if (getSocket() == null) {
                throw new UnsupportedOperationException("unsupported option");
            }
            if (isClosedOrPending()) {
                throw new SocketException("Socket closed");
            }
            return (T) extendedOptions.getOption(fd, name);
        }
    }

    protected Set<SocketOption<?>> supportedOptions() {
        HashSet<SocketOption<?>> options = new HashSet<>(super.supportedOptions());
        if (getSocket() != null) {
            options.addAll(extendedOptions.options());
        }
        return options;
    }

    protected void socketSetOption(int opt, boolean b, Object val) throws SocketException {
        if (opt == SocketOptions.SO_REUSEPORT &&
            !supportedOptions().contains(StandardSocketOptions.SO_REUSEPORT)) {
            throw new UnsupportedOperationException("unsupported option");
        }
        try {
            socketSetOption0(opt, b, val);
        } catch (SocketException se) {
            if (socket == null || !socket.isConnected())
                throw se;
        }
    }

    native void socketCreate(boolean isServer) throws IOException;

    native void socketConnect(InetAddress address, int port, int timeout)
        throws IOException;

    native void socketBind(InetAddress address, int port)
        throws IOException;

    native void socketListen(int count) throws IOException;

    native void socketAccept(SocketImpl s) throws IOException;

    native int socketAvailable() throws IOException;

    native void socketClose0(boolean useDeferredClose) throws IOException;

    native void socketShutdown(int howto) throws IOException;

    static native void initProto();

    native void socketSetOption0(int cmd, boolean on, Object value)
        throws SocketException;

    native int socketGetOption(int opt, Object iaContainerObj) throws SocketException;

    native void socketSendUrgentData(int data) throws IOException;
}