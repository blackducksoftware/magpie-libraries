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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Additional support for RFC 4122 Universally Unique IDentifiers.
 *
 * @author jgustie
 */
public class ExtraUUIDs {

    // TODO SHA-1 name-based UUIDs (version 5) should be preferred

    /**
     * The nil UUID is special form of UUID that is specified to have all 128 bits set to zero.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4122#section-4.1.7">RFC 4122 Section 4.1.7</a>
     */
    private static final UUID NIL = new UUID(0, 0);

    /**
     * Name string is a fully-qualified domain name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4122#appendix-C">RFC 4122 Appendix C</a>
     */
    private static final UUID NAME_SPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Name string is a URL.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4122#appendix-C">RFC 4122 Appendix C</a>
     */
    private static final UUID NAME_SPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Name string is an ISO OID.
     *
     * @see <a href="https://tools.ietf.org/html/rfc4122#appendix-C">RFC 4122 Appendix C</a>
     */
    private static final UUID NAME_SPACE_OID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Name string is an X.500 DN (in DER or a text output format).
     *
     * @see <a href="https://tools.ietf.org/html/rfc4122#appendix-C">RFC 4122 Appendix C</a>
     */
    private static final UUID NAME_SPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Returns the special nil UUID.
     */
    public static UUID nilUUID() {
        return NIL;
    }

    /**
     * Returns the supplied UUID as URN in the "uuid" namespace.
     */
    public static String toUriString(UUID uuid) {
        return "urn:uuid:" + uuid;
    }

    /**
     * Returns a URI representing the supplied UUID.
     *
     * @see #toUriString(UUID)
     */
    public static URI toUri(UUID uuid) {
        return URI.create(toUriString(uuid));
    }

    /**
     * Relative put method for writing a UUID value to a byte buffer.
     */
    public static ByteBuffer putUUID(ByteBuffer bb, UUID uuid) {
        return bb.putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
    }

    /**
     * Creates a new UUID from the supplied namespace and name. This may be preferable to the
     * {@code UUID.nameUUIDFromBytes} method which does not separate the name space ID from the name.
     */
    public static UUID nameUUIDFromBytes(UUID nameSpaceId, byte[] name) {
        byte[] nameBytes = new byte[16 + name.length];
        putUUID(ByteBuffer.wrap(nameBytes), nameSpaceId).put(name);
        return UUID.nameUUIDFromBytes(nameBytes);
    }

    /**
     * Creates a new UUID from the supplied namespace and name.
     *
     * @see #nameUUIDFromBytes(UUID, byte[])
     * @implNote Primarily overloaded to avoid excess array copying when encoding character sequences
     */
    public static UUID nameUUIDFromBytes(UUID nameSpaceId, ByteBuffer name) {
        byte[] nameBytes = new byte[16 + name.remaining()];
        putUUID(ByteBuffer.wrap(nameBytes), nameSpaceId).put(name);
        return UUID.nameUUIDFromBytes(nameBytes);
    }

    /**
     * Returns a name-based (version 3) UUID from a fully qualified domain name.
     */
    public static UUID nameUUIDFromDns(String fullyQualifiedDomainName) {
        return nameUUIDFromBytes(NAME_SPACE_DNS, UTF_8.encode(fullyQualifiedDomainName));
    }

    /**
     * Returns a name-based (version 3) UUID from a URL.
     */
    public static UUID nameUUIDFromUrl(String url) {
        return nameUUIDFromBytes(NAME_SPACE_URL, UTF_8.encode(url));
    }

    /**
     * Returns a name-based (version 3) UUID from an ISO OID.
     */
    public static UUID nameUUIDFromOid(String oid) {
        return nameUUIDFromBytes(NAME_SPACE_OID, US_ASCII.encode(oid));
    }

    /**
     * Returns a name-based (version 3) UUID from an X.500 name (text representation).
     */
    public static UUID nameUUIDFromX500(String dn) {
        return nameUUIDFromBytes(NAME_SPACE_X500, UTF_8.encode(dn));
    }

    /**
     * Returns a name-based (version 3) UUID from an X.500 name (DER encoded).
     */
    public static UUID nameUUIDFromX500(byte[] dn) {
        return nameUUIDFromBytes(NAME_SPACE_X500, dn);
    }

    private ExtraUUIDs() {
        assert false;
    }
}
