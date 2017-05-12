/*
 * Copyright 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.common.security.auth;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.Optional;
import java.util.ResourceBundle;

import com.blackducksoftware.common.security.auth.InvalidCertificateCallback.Choice;

/**
 * Base class for asking users how they want to proceed with an invalid certificate.
 *
 * @author jgustie
 */
public abstract class InvalidCertificateCallback extends EnumChoiceCallback<Choice> {

    /**
     * The levels of trust to apply to the untrusted certificate chain.
     */
    public enum Choice {
        NEVER,
        ONCE,
        ALWAYS,
    }

    /**
     * The default resource bundle to use for this callback.
     */
    public static final class InvalidCertificateCallbackBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                    { "NEVER", "Cancel" },
                    { "ONCE", "Continue" },
                    { "ALWAYS", "Always Connect" },
                    { "prompt", "Cannot verify the identity of the server \"{0}\". "
                            + "The certificate for this server is invalid. Would you like to connect anyway?" },
            };
        }
    }

    protected InvalidCertificateCallback(String host) {
        super(prompt(host), Choice.class, Choice.NEVER, false,
                // TODO This is going to need to be more configurable
                ResourceBundle.getBundle(InvalidCertificateCallbackBundle.class.getName()));
    }

    /**
     * Generates a prompt based on the certificate chain.
     */
    private static final String prompt(String host) {
        ResourceBundle bundle = ResourceBundle.getBundle(InvalidCertificateCallbackBundle.class.getName());
        return MessageFormat.format(bundle.getString("prompt"), host);
    }

    /**
     * Returns the trust level selected by the user. Never returns empty.
     */
    @Override
    public Optional<Choice> getSelectedChoice() {
        // WTF. No `Optional.or(Optional<T>)`?
        Optional<Choice> selectedChoice = super.getSelectedChoice();
        return selectedChoice.isPresent() ? selectedChoice : Optional.of(Choice.NEVER);
    }

}
