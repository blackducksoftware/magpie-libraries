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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;

import org.junit.Test;

/**
 * Basic tests for the {@code KeyPairStore}.
 *
 * @author jgustie
 */
public class KeyPairStoreBasicTest extends AbstractSecurityTest {

    // NOTES:
    // * No parameters to the builder can be null, but the password in a PasswordProtection can be null
    // * Only password and callback are allowed for protection parameters

    @Test(expected = NullPointerException.class)
    public void builderNullPointer_alias() {
        KeyPairStore.Builder.newInstance(null, mock(Path.class), mock(Path.class), new PasswordProtection(null));
    }

    @Test(expected = NullPointerException.class)
    public void builderNullPointer_keyFile() {
        KeyPairStore.Builder.newInstance("tls", null, mock(Path.class), new PasswordProtection(null));
    }

    @Test(expected = NullPointerException.class)
    public void builderNullPointer_certificateFile() {
        KeyPairStore.Builder.newInstance("tls", mock(Path.class), null, new PasswordProtection(null));
    }

    @Test(expected = NullPointerException.class)
    public void builderNullPointer_protection() {
        KeyPairStore.Builder.newInstance("tls", mock(Path.class), mock(Path.class), null);
    }

    @Test
    public void builderIllegalArgument_protection() {
        try {
            KeyPairStore.Builder.newInstance("tls", mock(Path.class), mock(Path.class), new ProtectionParameter() {
            }).getKeyStore();
            fail();
        } catch (KeyStoreException e) {
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
            assertThat(e.getCause()).hasMessage("Protection must be PasswordProtection or CallbackHandlerProtection");
        }
    }

}
