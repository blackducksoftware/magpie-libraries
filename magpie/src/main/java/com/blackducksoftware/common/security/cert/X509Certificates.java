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
package com.blackducksoftware.common.security.cert;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static javax.security.auth.x500.X500Principal.RFC2253;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import com.blackducksoftware.common.base.ExtraStreams;

/**
 * Helpers for working with X.509 certificates.
 *
 * @author jgustie
 */
public class X509Certificates {

    /**
     * @see X509Certificate#getSubjectAlternativeNames()
     */
    private enum GeneralName {

        other(0, false),

        /**
         * Also known as "email".
         */
        rfc822Name(1, true),

        /**
         * Commonly used for server certificates as the allowed host name.
         */
        dNSName(2, true),

        x400Address(3, false),

        directoryName(4, true),

        ediPartyName(5, false),

        uniformResourceIdentifier(6, true),

        iPAddress(7, true),

        registeredID(8, true),

        ;

        private final Integer index;

        private final boolean string;

        private GeneralName(int i, boolean string) {
            this.index = Integer.valueOf(i);
            this.string = string;
        }

        public Stream<String> toString(List<?> generalName) {
            checkState(string, "cannot call toString(List<?>) on %s", name());
            verifyGeneralName(generalName);
            return generalName.get(0).equals(index) ? Stream.of((String) generalName.get(1)) : Stream.empty();
        }

        public Stream<byte[]> toByteArray(List<?> generalName) {
            checkState(!string, "cannot call toByteArray(List<?>) on %s", name());
            verifyGeneralName(generalName);
            return generalName.get(0).equals(index) ? Stream.of((byte[]) generalName.get(1)) : Stream.empty();
        }

        private static void verifyGeneralName(List<?> generalName) {
            verify(generalName.size() == 2);
            verify(generalName.get(0) instanceof Integer);
            verify(generalName.get(1) instanceof String || generalName.get(1) instanceof byte[]);
        }
    }

    /**
     * Attempts to extract the common name from the certificate's subject.
     */
    public static Optional<String> subjectCommonName(X509Certificate certificate) {
        try {
            LdapName subjectName = new LdapName(certificate.getSubjectX500Principal().getName(RFC2253));
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

    /**
     * Attempts to extract the other names from the certificate's subject alternative names.
     */
    public static Stream<byte[]> subjectAlternativeOtherNames(X509Certificate certificate) {
        return subjectAlternativeNames(certificate).flatMap(GeneralName.other::toByteArray);
    }

    /**
     * Attempts to extract the RFC 822 names (email addresses) from the certificate's subject alternative names.
     */
    public static Stream<String> subjectAlternativeRfc822Names(X509Certificate certificate) {
        return subjectAlternativeNames(certificate).flatMap(GeneralName.rfc822Name::toString);
    }

    /**
     * Attempts to extract the DNS names (hostnames) from the certificate's subject alternative names.
     */
    public static Stream<String> subjectAlternativeDnsNames(X509Certificate certificate) {
        return subjectAlternativeNames(certificate).flatMap(GeneralName.dNSName::toString);
    }

    /**
     * Internal helper to stream the subject alternative names from a certificate.
     */
    private static Stream<List<?>> subjectAlternativeNames(X509Certificate certificate) {
        try {
            return ExtraStreams.streamNullable(certificate.getSubjectAlternativeNames());
        } catch (CertificateParsingException e) {
            return Stream.empty();
        }
    }

    private X509Certificates() {
        assert false;
    }
}
