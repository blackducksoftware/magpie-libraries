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

import static com.blackducksoftware.common.security.cert.X509Certificates.subjectAlternativeDnsNames;
import static com.blackducksoftware.common.security.cert.X509Certificates.subjectCommonName;

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

import com.blackducksoftware.common.base.ExtraOptionals;
import com.blackducksoftware.common.base.ExtraStrings;
import com.google.common.net.InternetDomainName;

/**
 * An invalid certificate callback for when the certificate chain cannot be verified.
 *
 * @author jgustie
 */
public final class InvalidCertificateChainCallback extends InvalidCertificateCallback {

    private static final long serialVersionUID = -374147648020177306L;

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
     * Extracts the most likely server name from a certificate.
     */
    private static String getName(X509Certificate cert) {
        Optional<String> name = Optional.empty();
        name = ExtraOptionals.or(name, () -> subjectAlternativeDnsNames(cert).findFirst());
        name = ExtraOptionals.or(name, () -> subjectCommonName(cert).filter(InternetDomainName::isValid));
        name = ExtraOptionals.or(name, () -> ExtraStrings.ofEmpty(cert.getSubjectX500Principal().getName()));
        return name.orElse("<unknown host>");
    }

}
