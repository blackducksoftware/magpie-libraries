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
package com.blackducksoftware.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * A forwarding socket which allows socket behaviors to be overridden.
 *
 * @author jgustie
 */
public abstract class ForwardingSocket extends Socket {

    /**
     * A simple wrapper of a single socket.
     */
    public static class SimpleForwardingSocket extends ForwardingSocket {

        private final Socket delegate;

        protected SimpleForwardingSocket(Socket delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        protected final Socket delegate() {
            return delegate;
        }
    }

    protected ForwardingSocket() {
    }

    /**
     * Returns the backing delegate socket that is being wrapped.
     */
    protected abstract Socket delegate();

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        delegate().connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        delegate().connect(endpoint, timeout);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        delegate().bind(bindpoint);
    }

    @Override
    public InetAddress getInetAddress() {
        return delegate().getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress() {
        return delegate().getLocalAddress();
    }

    @Override
    public int getPort() {
        return delegate().getPort();
    }

    @Override
    public int getLocalPort() {
        return delegate().getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return delegate().getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return delegate().getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return delegate().getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return delegate().getOutputStream();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        delegate().setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return delegate().getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        delegate().setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return delegate().getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        delegate().sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        delegate().setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return delegate().getOOBInline();
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        delegate().setSoTimeout(timeout);
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return delegate().getSoTimeout();
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        delegate().setSendBufferSize(size);
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return delegate().getSendBufferSize();
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        delegate().setReceiveBufferSize(size);
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return delegate().getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        delegate().setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return delegate().getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        delegate().setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return delegate().getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        delegate().setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return delegate().getReuseAddress();
    }

    @Override
    public void close() throws IOException {
        delegate().close();
    }

    @Override
    public void shutdownInput() throws IOException {
        delegate().shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        delegate().shutdownOutput();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

    @Override
    public boolean isConnected() {
        return delegate().isConnected();
    }

    @Override
    public boolean isBound() {
        return delegate().isBound();
    }

    @Override
    public boolean isClosed() {
        return delegate().isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return delegate().isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return delegate().isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        delegate().setPerformancePreferences(connectionTime, latency, bandwidth);
    }

}
