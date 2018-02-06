/*
 * Copyright 2018 Synopsys, Inc.
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
package com.blackducksoftware.common.net;

import static com.blackducksoftware.common.base.ExtraStrings.beforeFirst;
import static java.net.IDN.USE_STD3_ASCII_RULES;

import java.net.IDN;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import com.blackducksoftware.common.base.ExtraStreams;
import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;

/**
 * Hostname utilities.
 *
 * @author jgustie
 */
@Beta
public class Hostname {

    /**
     * Hostname to use for a loopback address.
     */
    private static final String LOOPBACK_HOSTNAME = "localhost";

    /**
     * Returns the local hostname. If you are going to use this in conjunction with {@code java.net.URI}, consider
     * {@link #getAsciiHostname()} instead.
     */
    public static String get() {
        try {
            return hostname(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            return allInetAddresses()
                    .map(Hostname::hostname)
                    .filter(Hostname::isNotInetAddress)
                    .findFirst().orElse(LOOPBACK_HOSTNAME);
        }
    }

    /**
     * Returns the ASCII (LDH only) hostname. If the ASCII representation contains non-LDH characters, the textual IP
     * address is returned instead, making this method safe for use with the dated {@code java.net.URI} parser.
     */
    public static String getAscii() {
        try {
            return IDN.toASCII(get(), USE_STD3_ASCII_RULES);
        } catch (IllegalArgumentException e) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ee) {
                return allInetAddresses()
                        .map(InetAddress::getHostAddress)
                        .findFirst().orElse(InetAddress.getLoopbackAddress().getHostAddress());
            }
        }
    }

    /**
     * Return the preferred hostname for the supplied address.
     */
    private static String hostname(InetAddress addr) {
        if (addr.isLoopbackAddress()) {
            return LOOPBACK_HOSTNAME;
        } else {
            String fqdn = addr.getCanonicalHostName();
            return isNotInetAddress(fqdn) ? fqdn : addr.getHostName();
        }
    }

    /**
     * Check to ensure the supplied string is not a formatted IP address.
     */
    private static boolean isNotInetAddress(String addr) {
        return !InetAddresses.isInetAddress(beforeFirst(addr, '%'));
    }

    /**
     * Returns a stream of (almost) all of the known addresses for this machine.
     */
    private static Stream<InetAddress> allInetAddresses() {
        try {
            return ExtraStreams.stream(NetworkInterface.getNetworkInterfaces())
                    .filter(ni -> {
                        try {
                            return ni.isUp() && !ni.isLoopback();
                        } catch (SocketException e) {
                            return false;
                        }
                    })
                    .flatMap(ni -> ExtraStreams.stream(ni.getInetAddresses()));
        } catch (SocketException e) {
            return Stream.empty();
        }
    }

    private Hostname() {
        assert false;
    }

}
