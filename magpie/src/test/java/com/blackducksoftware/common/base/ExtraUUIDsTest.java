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
package com.blackducksoftware.common.base;

import static com.google.common.truth.Truth.assertThat;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.security.auth.x500.X500Principal;

import org.junit.Test;

/**
 * Tests for {@code ExtraUUIDs}.
 *
 * @author jgustie
 */
public class ExtraUUIDsTest {

    /**
     * Expected output for a version 3 UUID with a DNS name of "www.widgets.com".
     * <p>
     * Note that the RFC states this should be "e902893a-9d22-3c7e-a7b8-d6e313b71d9f" which is incorrect.
     *
     * @implSpec {@code uuid -v3 ns:DNS www.widgets.com}
     * @see <https://www.rfc-editor.org/errata/eid1352>Errata 1352</a>
     */
    private static final String UUID_OUTPUT_V3_DNS_WWW_WIDGETS_COM = "3d813cbb-47fb-32ba-91df-831e1593ac29";

    /**
     * Expected output for a version 3 UUID with an X.500 name of "DC=com,DC=example,CN=Nobody".
     *
     * @implSpec {@code uuid -v3 ns:X500 DC=com,DC=example,CN=Nobody}
     */
    private static final String UUID_OUTPUT_V3_X500_DC_COM_DC_EXAMPLE_CN_NOBDOY = "5bef6a07-77f3-3874-aeb7-e271ce5e53e1";

    @Test
    public void nil() {
        assertThat(ExtraUUIDs.nilUUID().getMostSignificantBits()).isEqualTo(0L);
        assertThat(ExtraUUIDs.nilUUID().getLeastSignificantBits()).isEqualTo(0L);
    }

    @Test
    public void uri() {
        UUID uuid = UUID.randomUUID();
        URI uri = ExtraUUIDs.toUri(uuid);
        assertThat(uri.getScheme()).isEqualTo("urn");
        assertThat(uri.getSchemeSpecificPart()).isEqualTo("uuid:" + uuid);
    }

    @Test
    public void fromUriString() {
        UUID uuid = UUID.randomUUID();
        assertThat(ExtraUUIDs.fromUriString("URN:UUID:" + uuid)).isEqualTo(uuid);
        assertThat(ExtraUUIDs.fromUriString("URN:uuid:" + uuid)).isEqualTo(uuid);
        assertThat(ExtraUUIDs.fromUriString("urn:uuid:" + uuid)).isEqualTo(uuid);
    }

    @Test
    public void fromUri() {
        UUID uuid = UUID.randomUUID();
        assertThat(ExtraUUIDs.fromUri(URI.create("URN:UUID:" + uuid))).isEqualTo(uuid);
        assertThat(ExtraUUIDs.fromUri(URI.create("URN:uuid:" + uuid))).isEqualTo(uuid);
        assertThat(ExtraUUIDs.fromUri(URI.create("urn:uuid:" + uuid))).isEqualTo(uuid);
    }

    @Test
    public void fromString_lowerCase() {
        assertThat(ExtraUUIDs.fromString("00000000-0000-0000-0000-000000000000")).isEqualTo(new UUID(0x00000000_00000000L, 0x00000000_00000000L));
        assertThat(ExtraUUIDs.fromString("01234567-89ab-cdef-0123-456789abcdef")).isEqualTo(new UUID(0x01234567_89abcdefL, 0x01234567_89abcdefL));
        assertThat(ExtraUUIDs.fromString("3d813cbb-47fb-32ba-91df-831e1593ac29")).isEqualTo(new UUID(0x3d813cbb_47fb32baL, 0x91df831e_1593ac29L));
        assertThat(ExtraUUIDs.fromString("5bef6a07-77f3-3874-aeb7-e271ce5e53e1")).isEqualTo(new UUID(0x5bef6a07_77f33874L, 0xaeb7e271_ce5e53e1L));
        assertThat(ExtraUUIDs.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")).isEqualTo(new UUID(0xFFFFFFFF_FFFFFFFFL, 0xFFFFFFFF_FFFFFFFFL));
    }

    @Test
    public void fromString_upperCase() {
        assertThat(ExtraUUIDs.fromString("00000000-0000-0000-0000-000000000000")).isEqualTo(new UUID(0x00000000_00000000L, 0x00000000_00000000L));
        assertThat(ExtraUUIDs.fromString("01234567-89AB-CDEF-0123-456789ABCDEF")).isEqualTo(new UUID(0x01234567_89abcdefL, 0x01234567_89abcdefL));
        assertThat(ExtraUUIDs.fromString("3D813CBB-47FB-32BA-91DF-831E1593AC29")).isEqualTo(new UUID(0x3d813cbb_47fb32baL, 0x91df831e_1593ac29L));
        assertThat(ExtraUUIDs.fromString("5BEF6A07-77F3-3874-AEB7-E271CE5E53E1")).isEqualTo(new UUID(0x5bef6a07_77f33874L, 0xaeb7e271_ce5e53e1L));
        assertThat(ExtraUUIDs.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")).isEqualTo(new UUID(0xffffffff_ffffffffL, 0xffffffff_ffffffffL));
    }

    @Test(expected = NullPointerException.class)
    public void fromString_null() {
        ExtraUUIDs.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_empty() {
        ExtraUUIDs.fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_tooShort() {
        ExtraUUIDs.fromString("01234567-89AB-CDEF-0123-456789ABCDE");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_tooLong() {
        ExtraUUIDs.fromString("01234567-89AB-CDEF-0123-456789ABCDEF0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_wrongDash8() {
        ExtraUUIDs.fromString("012345678-9AB-CDEF-0123-456789ABCDEF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_wrongDashC() {
        ExtraUUIDs.fromString("01234567-89ABC-DEF-0123-456789ABCDEF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_wrongDash0() {
        ExtraUUIDs.fromString("01234567-89AB-CDEF0-123-456789ABCDEF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromString_wrongDash4() {
        ExtraUUIDs.fromString("01234567-89AB-CDEF-01234-56789ABCDEF");
    }

    @Test(expected = NumberFormatException.class)
    public void fromString_numberG() {
        ExtraUUIDs.fromString("01234567-89AB-CDEF-0123-456789ABCDEG");
    }

    @Test
    public void rfc4122AppendixB() {
        assertThat(ExtraUUIDs.nameUUIDFromDns("www.widgets.com").toString()).isEqualTo(UUID_OUTPUT_V3_DNS_WWW_WIDGETS_COM);
    }

    @Test
    public void byteBuffer() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.allocate(16);
        ExtraUUIDs.putUUID(bb, uuid).flip();
        assertThat(bb.getLong()).isEqualTo(uuid.getMostSignificantBits());
        assertThat(bb.getLong()).isEqualTo(uuid.getLeastSignificantBits());
    }

    @Test
    public void x500Name() {
        X500Principal name = new X500Principal("DC=com,DC=example,CN=Nobody");
        assertThat(ExtraUUIDs.nameUUIDFromX500(name.getName()).toString()).isEqualTo(UUID_OUTPUT_V3_X500_DC_COM_DC_EXAMPLE_CN_NOBDOY);
        assertThat(ExtraUUIDs.nameUUIDFromX500(name.getEncoded())).isNotEqualTo(ExtraUUIDs.nameUUIDFromX500(name.getName()));
    }

}
