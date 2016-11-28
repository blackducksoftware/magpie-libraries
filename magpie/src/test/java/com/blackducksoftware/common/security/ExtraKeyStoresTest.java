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
package com.blackducksoftware.common.security;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.Test;

/**
 * Tests for {@code ExtraKeyStores}.
 *
 * @author jgustie
 */
public class ExtraKeyStoresTest extends AbstractSecurityTest {

    private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();

    @Test
    public void keyStoreAlgorithm_jks() throws IOException {
        assertThat(ExtraKeyStores.guessKeyStoreAlgorithm(file("user.jks"))).isEqualTo("jks");
    }

    @Test
    public void keyStoreAlgorithm_jceks() throws IOException {
        assertThat(ExtraKeyStores.guessKeyStoreAlgorithm(file("user.jceks"))).isEqualTo("jceks");
    }

    @Test
    public void keyStoreAlgorithm_pkcs12() throws IOException {
        assertThat(ExtraKeyStores.guessKeyStoreAlgorithm(file("user.p12"))).isEqualTo("pkcs12");
    }

    @Test
    public void keyStoreAlgorithm_pk8_pem() throws IOException {
        assertThat(ExtraKeyStores.guessKeyStoreAlgorithm(file("privkey.pk8.pem"))).isNull();
    }

    @Test
    public void getOnlyKeyProtection_password() throws Exception {
        KeyStore.Builder builder = KeyStore.Builder.newInstance("jks", null, file("user.jks").toFile(), new KeyStore.PasswordProtection(DEFAULT_PASSWORD));
        assertThat(ExtraKeyStores.getOnlyKeyProtection(builder).getPassword()).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test
    public void getOnlyKeyProtection_callback() throws Exception {
        KeyStore.Builder builder = KeyStore.Builder.newInstance("jks", null, file("user.jks").toFile(), new KeyStore.CallbackHandlerProtection(callbacks -> {
            for (Callback callback : callbacks) {
                if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(DEFAULT_PASSWORD);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }));
        assertThat(ExtraKeyStores.getOnlyKeyProtection(builder).getPassword()).isEqualTo(DEFAULT_PASSWORD);
    }

    @Test(expected = KeyStoreException.class)
    public void getOnlyKeyProtection_emptyKeyStore() throws KeyStoreException {
        ExtraKeyStores.getOnlyKeyProtection(KeyStore.Builder.newInstance("jks", null, new KeyStore.PasswordProtection(DEFAULT_PASSWORD)));
    }

}
