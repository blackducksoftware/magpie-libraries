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
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Cipher;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.junit.Test;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

/**
 * Tests for the key pair store.
 *
 * @author jgustie
 */
public class KeyPairStoreTest extends AbstractSecurityTest {

    // AES 192 requires unlimited strength cryptography policy files
    @Test(expected = UnrecoverableKeyException.class)
    public void newInstance_privkey_aes192_pem() throws Exception {
        assume().that(Cipher.getMaxAllowedKeyLength("AES")).isEqualTo(128);

        checkKeyStore(file("privkey.aes192.pem"), file("cert.pem"), password("changeit"));
    }

    @Test
    public void newInstance_privkey_clear_der() throws Exception {
        checkKeyStore(file("privkey.clear.der"), file("cert.pem"), password(null));
    }

    @Test
    public void newInstance_privkey_clear_pem() throws Exception {
        checkKeyStore(file("privkey.clear.pem"), file("cert.pem"), password(null));
    }

    @Test
    public void newInstance_privkey_pem() throws Exception {
        checkKeyStore(file("privkey.pem"), file("cert.pem"), password("changeit"));
    }

    @Test
    public void newInstance_privkey_pk8_clear_der() throws Exception {
        checkKeyStore(file("privkey.pk8.clear.der"), file("cert.pem"), password(null));
    }

    @Test
    public void newInstance_privkey_pk8_clear_pem() throws Exception {
        checkKeyStore(file("privkey.pk8.clear.pem"), file("cert.pem"), password(null));
    }

    @Test
    public void newInstance_privkey_pk8_der() throws Exception {
        checkKeyStore(file("privkey.pk8.der"), file("cert.pem"), password("changeit"));
    }

    // This is a PBES2 encrypted key which Java does not have built in support for
    // https://tools.ietf.org/html/rfc2898#section-6.2
    @Test(expected = UnrecoverableKeyException.class)
    public void newInstance_privkey_pk8_des3_der() throws Exception {
        checkKeyStore(file("privkey.pk8.des3.der"), file("cert.pem"), password("changeit"));
    }

    // This is a PBES2 encrypted key which Java does not have built in support for
    // https://tools.ietf.org/html/rfc2898#section-6.2
    @Test(expected = UnrecoverableKeyException.class)
    public void newInstance_privkey_pk8_des3_pem() throws Exception {
        checkKeyStore(file("privkey.pk8.des3.pem"), file("cert.pem"), password("changeit"));
    }

    @Test
    public void newInstance_privkey_pk8_pem() throws Exception {
        checkKeyStore(file("privkey.pk8.pem"), file("cert.pem"), password("changeit"));
    }

    @Test
    public void loadPasswordProtection() throws Exception {
        KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.pk8.pem"), file("cert.pem"),
                new PasswordProtection("changeit".toCharArray()));
        KeyStore keyStore = builder.getKeyStore();
        assertThat(builder.getKeyStore()).isSameAs(keyStore);
        assertThat(keyStore.containsAlias("user")).isTrue();
        assertThat(keyStore.isKeyEntry("user")).isTrue();
        assertThat(keyStore.size()).isEqualTo(1);
    }

    @Test
    public void loadCallbackProtection() throws Exception {
        KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.pk8.pem"), file("cert.pem"),
                new CallbackHandlerProtection(new RetryCallbackHandler(0)));
        KeyStore keyStore = builder.getKeyStore();
        assertThat(builder.getKeyStore()).isSameAs(keyStore);
        assertThat(keyStore.containsAlias("user")).isTrue();
        assertThat(keyStore.isKeyEntry("user")).isTrue();
        assertThat(keyStore.size()).isEqualTo(1);
    }

    @Test
    public void loadCallbackProtection_oneFailed() throws Exception {
        KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.pk8.pem"), file("cert.pem"),
                new CallbackHandlerProtection(new RetryCallbackHandler(1)));
        builder.getKeyStore();
    }

    @Test(expected = KeyStoreException.class)
    public void loadCallbackProtection_threeFailed_Pkcs8Pem() throws Exception {
        KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.pk8.pem"), file("cert.pem"),
                new CallbackHandlerProtection(new RetryCallbackHandler(3)));
        builder.getKeyStore();
        // Don't load the key, it might mask problems
    }

    @Test(expected = KeyStoreException.class)
    public void loadCallbackProtection_threeFailed_Pkcs1Pem() throws Exception {
        KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.pem"), file("cert.pem"),
                new CallbackHandlerProtection(new RetryCallbackHandler(3)));
        builder.getKeyStore();
        // Don't load the key, it might mask problems
    }

    @Test(expected = KeyStoreException.class)
    public void loadCallbackProtection_threeFailed_Pkcs1Der() throws Exception {
        KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.der"), file("cert.pem"),
                new CallbackHandlerProtection(new RetryCallbackHandler(3)));
        builder.getKeyStore();
        // Don't load the key, it might mask problems
    }

    @Test
    public void loadCallbackProtection_nullPassword() throws Exception {
        try {
            KeyPairStore.Builder builder = KeyPairStore.Builder.newInstance("user", file("privkey.pk8.pem"), file("cert.pem"),
                    new CallbackHandlerProtection(new RetryCallbackHandler(-1)));
            builder.getKeyStore();
            fail();
        } catch (KeyStoreException e) {
            assertThat(e).hasMessageThat().isEqualTo("No password provided");
        }
    }

    private static void checkKeyStore(Path privateKeyFile, Path certificateFile, ProtectionParameter protection) throws Exception {
        String alias = "user";
        KeyStore.Builder builder = KeyPairStore.Builder.newInstance(alias, privateKeyFile, certificateFile, protection);
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) builder.getKeyStore().getEntry(alias, builder.getProtectionParameter(alias));
        Cipher cipher = Cipher.getInstance(entry.getPrivateKey().getAlgorithm());

        cipher.init(Cipher.ENCRYPT_MODE, entry.getPrivateKey());
        assertThat(cipher.doFinal(plaintext().read())).isEqualTo(ciphertext().read());

        cipher.init(Cipher.DECRYPT_MODE, entry.getCertificate());
        assertThat(cipher.doFinal(ciphertext().read())).isEqualTo(plaintext().read());
    }

    private static ProtectionParameter password(String password) {
        return new PasswordProtection(password != null ? password.toCharArray() : null);
    }

    private static ByteSource plaintext() {
        return Resources.asByteSource(Resources.getResource(KeyPairStoreTest.class, "plaintext.txt"));
    }

    private static ByteSource ciphertext() {
        URL ciphertext = Resources.getResource(KeyPairStoreTest.class, "ciphertext.txt");
        return BaseEncoding.base64().withSeparator("\n", 64).decodingSource(Resources.asCharSource(ciphertext, StandardCharsets.US_ASCII));
    }

    private static class RetryCallbackHandler implements CallbackHandler {
        private final AtomicReference<PasswordCallback> callback = new AtomicReference<>();

        private int remainingAttempts;

        public RetryCallbackHandler(int remainingAttempts) {
            this.remainingAttempts = remainingAttempts;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof PasswordCallback) {
                    if (this.callback.compareAndSet(null, (PasswordCallback) callback)) {
                        if (remainingAttempts < 0) {
                            this.callback.get().setPassword(null);
                        } else if (remainingAttempts > 0) {
                            if (remainingAttempts == 1) {
                                this.callback.get().setPassword("longerthenrealpassword".toCharArray());
                            } else if (remainingAttempts == 2) {
                                this.callback.get().setPassword("short".toCharArray());
                            } else {
                                this.callback.get().setPassword("sameleng".toCharArray());
                            }
                            this.callback.set(null);
                            remainingAttempts--;
                        } else {
                            this.callback.get().setPassword("changeit".toCharArray());
                        }
                    } else {
                        // We wouldn't want to ask for the password over and over again...
                        fail("CallbackHandler invoked multipe times");
                    }
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }

}
