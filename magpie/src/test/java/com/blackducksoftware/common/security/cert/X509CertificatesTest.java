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

import static com.google.common.truth.Truth8.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.google.common.io.Resources;

/**
 * Tests for {@code X509Certificates}.
 *
 * @author jgustie
 */
public class X509CertificatesTest {

    private static final Map<String, X509Certificate> CERTIFICATE_CACHE = new ConcurrentHashMap<>();

    private static X509Certificate loadCert(String resourceName) {
        return CERTIFICATE_CACHE.computeIfAbsent(resourceName, n -> {
            try (InputStream in = Resources.asByteSource(Resources.getResource(X509CertificatesTest.class, n)).openStream()) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                return (X509Certificate) factory.generateCertificate(in);
            } catch (IOException | CertificateException e) {
                throw new AssertionError("Unable to load certificate test data for: " + resourceName, e);
            }
        });
    }

    @Test
    public void subjectCommonName() {
        assertThat(X509Certificates.subjectCommonName(loadCert("server_single_san.pem"))).hasValue("example.com");
    }

    @Test
    public void subjectAlternativeDnsNames() {
        assertThat(X509Certificates.subjectAlternativeDnsNames(loadCert("server_single_san.pem"))).containsExactly("example.com");
    }

    @Test
    public void subjectAlternativeOtherNames() {
        assertThat(X509Certificates.subjectAlternativeOtherNames(loadCert("server_single_san.pem"))).isEmpty();
    }

}
