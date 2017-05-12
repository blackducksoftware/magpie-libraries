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

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.google.common.base.CharMatcher;

/**
 * An invalid certificate callback for when the certificate chain cannot be verified.
 *
 * @author jgustie
 */
public final class InvalidCertificateChainCallback extends InvalidCertificateCallback {

    private final X509Certificate[] chain;

    private final String authType;

    public InvalidCertificateChainCallback(X509Certificate[] chain, String authType) {
        super(getName(chain[0]));
        this.chain = Objects.requireNonNull(chain);
        this.authType = Objects.requireNonNull(authType);
    }

    /**
     * Returns the chain of certificates that could not be verified.
     */
    public X509Certificate[] getCertificateChain() {
        return chain;
    }

    /**
     * Returns the authentication type when the certificate chain was determined to be invalid.
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * Returns a key store alias to use for this trusted certificate chain.
     */
    public String getAlias() {
        // If things really go south, you can always set this system property
        return Optional.ofNullable(System.getProperty("blackduck.scan.untrustedCertAlias"))
                .orElseGet(() -> subjectCommonName(chain[0])
                        .map(CharMatcher.inRange('a', 'z')::retainFrom)
                        .map(String::toLowerCase)
                        .map(name -> name.concat("server")) // TODO Don't double up...
                        .orElse("untrustedserver"));
    }

    /**
     * Extracts the most likely server name from a certificate.
     */
    private static String getName(X509Certificate cert) {
        // TODO Subject alternative names first...
        return subjectCommonName(cert)
                .orElseGet(() -> cert.getSubjectDN().getName());
    }

    /**
     * Attempts to extract the common name from the certificate's subject.
     */
    private static Optional<String> subjectCommonName(X509Certificate cert) {
        try {
            LdapName subjectName = new LdapName(cert.getSubjectX500Principal().getName());
            for (Rdn rdn : subjectName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    return Optional.of(rdn.getValue().toString());
                }
            }
            return Optional.empty();
        } catch (InvalidNameException e) {
            return Optional.empty();
        }
    }

}
