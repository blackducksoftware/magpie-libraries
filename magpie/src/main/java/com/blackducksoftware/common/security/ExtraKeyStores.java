/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.common.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.util.Enumeration;

import javax.annotation.Nullable;

public final class ExtraKeyStores {

    /**
     * Attempts to guess the key store algorithm of a file based on magic numbers found in the file.
     */
    @Nullable
    public static String guessKeyStoreAlgorithm(Path path) throws IOException {
        try (ReadableByteChannel c = Files.newByteChannel(path)) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            if (c.read(buffer) == buffer.capacity()) {
                buffer.flip();
                switch (buffer.getInt()) {
                case 0xFEEDFEED:
                    return "jks";
                case 0xCECECECE:
                    return "jceks";
                default:
                    // This is just a sanity check, a lot of stuff can start with 0x30...
                    if (buffer.get(buffer.position() - 4) == 0x30) {
                        return "pkcs12";
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the password protection of the only key entry in the supplied key store builder.
     */
    public static PasswordProtection getOnlyKeyProtection(KeyStore.Builder keyStoreBuilder) throws KeyStoreException {
        PasswordProtection result = null;
        KeyStore keyStore = keyStoreBuilder.getKeyStore(); // Safe for multiple calls
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                // TODO Don't take this for granted and implement the convert logic here
                // This only works for password protection (since we need a PASSWORD). In most cases this
                // should happen automatically inside the key store builder (e.g. if we gave the builder
                // a callback handler, it would invoke it once and cache the result in a password
                // protection instance that is returned from getProtectionParameter)
                ProtectionParameter protection = keyStoreBuilder.getProtectionParameter(alias);
                if (protection instanceof PasswordProtection) {
                    if (result != null) {
                        throw new IllegalStateException("KeyStore contains multiple key entries");
                    } else {
                        result = (PasswordProtection) protection;
                    }
                } else {
                    // TODO Convert to a password protection if we can (then reuse this in KeyPairStore)
                    throw new IllegalStateException("KeyStore did not return password protection");
                }
            }
        }

        // Fail if we didn't find a protection parameter
        if (result != null) {
            return result;
        } else {
            throw new KeyStoreException("No key entry found");
        }
    }

    private ExtraKeyStores() {
        assert false;
    }
}
