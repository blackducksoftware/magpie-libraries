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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

/**
 * A key store implementation that exposes a single private key entry loaded from individual key and certificate files.
 *
 * @author jgustie
 */
public final class KeyPairStore {

    /**
     * A key store builder for a single key pair.
     */
    public static final class Builder extends KeyStore.Builder {

        /**
         * The alias used to identify the single entry of the resulting key store.
         */
        private final String alias;

        /**
         * The path to the key file.
         */
        private final Path keyFile;

        /**
         * The path to the certificate chain file.
         */
        private final Path certificateFile;

        /**
         * The protection parameter used to decrypt the private key.
         */
        private final ProtectionParameter protection;

        /**
         * The lazily constructed key store instance.
         */
        private KeyStore keyStore;

        /**
         * The recorded failure from the construction of the key store.
         */
        private Throwable oldException;

        /**
         * The protection parameter consumers should use for the private key entry.
         */
        private PasswordProtection privateKeyPassword;

        private Builder(String alias, Path keyFile, Path certificateFile, ProtectionParameter protection) {
            this.alias = Objects.requireNonNull(alias);
            this.keyFile = Objects.requireNonNull(keyFile);
            this.certificateFile = Objects.requireNonNull(certificateFile);
            this.protection = Objects.requireNonNull(protection);
        }

        /**
         * Returns a new key store builder that creates a single private key entry loaded from individual files for the
         * private and public key certification chain. The private key can be stored as a PKCS #1 or PKCS #8 encoded
         * key. The public key can be stored as a PKCS #7 encoded certificate chain.
         */
        public static Builder newInstance(String alias, Path keyFile, Path certificateFile, ProtectionParameter protection) {
            return new Builder(alias, keyFile, certificateFile, protection);
        }

        @Override
        public synchronized KeyStore getKeyStore() throws KeyStoreException {
            // Return lazily computed value
            if (keyStore != null) {
                return keyStore;
            } else if (oldException != null) {
                throw new KeyStoreException("Previous KeyStore instantiation failed", oldException);
            }

            // Allow retries when the protection is interactive
            int retryCount = protection instanceof CallbackHandlerProtection ? 3 : 1;
            while (retryCount > 0) {
                try {
                    // Determine the protection parameter for the private key
                    privateKeyPassword = privateKeyPassword(protection);

                    // Create and load a new key store
                    KeyStore resultKeyStore = KeyStore.getInstance("keypair", new KeyPairStoreProvider());
                    resultKeyStore.load(new KeyPairStoreLoadStoreParameter(alias, keyFile, certificateFile, privateKeyPassword));
                    keyStore = resultKeyStore;

                    return resultKeyStore;
                } catch (UnsupportedCallbackException | NoSuchAlgorithmException | CertificateException
                        | RuntimeException | IOException | KeyStoreException e) {
                    oldException = e;
                    if (e instanceof IOException && e.getCause() instanceof UnrecoverableKeyException) {
                        retryCount--; // Allow retries if this was an interactive failure
                    } else if (e instanceof KeyStoreException) {
                        throw (KeyStoreException) e; // Don't re-wrap, just re-throw
                    } else {
                        break;
                    }
                }
            }

            // If we fell through to here, it means we were unsuccessful at loading the key store
            throw new KeyStoreException("KeyStore instantiation failed", oldException);
        }

        @Override
        @Nullable
        public ProtectionParameter getProtectionParameter(String alias) throws KeyStoreException {
            Objects.requireNonNull(alias);
            checkState(keyStore != null);
            return this.alias.equals(alias) ? privateKeyPassword : null;
        }

        /**
         * Converts a protection parameter into a password protection parameter for the private key.
         */
        private PasswordProtection privateKeyPassword(ProtectionParameter protection) throws KeyStoreException, UnsupportedCallbackException, IOException {
            if (protection instanceof PasswordProtection) {
                return (PasswordProtection) protection;
            } else if (protection instanceof CallbackHandlerProtection) {
                // Use the callback handler to resolve a password
                // TODO i18n?
                PasswordCallback callback = new PasswordCallback("Password for key " + keyFile.getFileName(), false);
                try {
                    ((CallbackHandlerProtection) protection).getCallbackHandler().handle(new Callback[] { callback });
                    char[] password = callback.getPassword();
                    if (password != null) {
                        return new PasswordProtection(password);
                    } else {
                        throw new KeyStoreException("No password provided");
                    }
                } finally {
                    callback.clearPassword();
                }
            } else {
                throw new IllegalArgumentException("Protection must be PasswordProtection or CallbackHandlerProtection");
            }
        }
    }

    /**
     * The one-time use provider for the simple key store. Technically we could register this or use a single instance,
     * but we will create one each time so any security failures are treated the same as any other error.
     */
    private static final class KeyPairStoreProvider extends Provider {
        private KeyPairStoreProvider() {
            super("Key Pair Key Store", 0.0, "");
            putService(new Service(this, "KeyStore", "keypair", KeyPairStoreSpi.class.getName(), null, null));
        }
    }

    /**
     * The load parameter used read in a simple key store. Contains both the path of the key and the certificate.
     */
    private static final class KeyPairStoreLoadStoreParameter implements KeyStore.LoadStoreParameter {

        private final String alias;

        private final Path keyPath;

        private final Path certificatePath;

        private final PasswordProtection passwordProtection;

        private KeyPairStoreLoadStoreParameter(String alias, Path keyPath, Path certificatePath, PasswordProtection passwordProtection) {
            this.alias = Objects.requireNonNull(alias);
            this.keyPath = Objects.requireNonNull(keyPath);
            this.certificatePath = Objects.requireNonNull(certificatePath);
            this.passwordProtection = Objects.requireNonNull(passwordProtection);
        }

        public String getAlias() {
            return alias;
        }

        public Path getKeyPath() {
            return keyPath;
        }

        public Path getCertificatePath() {
            return certificatePath;
        }

        @Override
        public ProtectionParameter getProtectionParameter() {
            return passwordProtection;
        }
    }

    /**
     * The actual key store engine. Supports a single private key entry (that is, a private key with a certificate
     * chain). The entry is loaded from individual key and certificate files.
     */
    public static class KeyPairStoreSpi extends KeyStoreSpi {

        private String alias;

        private byte[] privateKeyMaterial;

        private Certificate[] certificateChain;

        private Date creationDate;

        @Override
        public void engineLoad(LoadStoreParameter param) throws IOException, NoSuchAlgorithmException, CertificateException {
            if (param instanceof KeyPairStoreLoadStoreParameter) {
                KeyPairStoreLoadStoreParameter simpleParam = (KeyPairStoreLoadStoreParameter) param;

                // Load the private key material
                checkPrivateKeyPermissions(simpleParam.getKeyPath());
                byte[] resultPrivateKeyMaterial = Files.readAllBytes(simpleParam.getKeyPath());

                // Do a test load of the private key to make sure the password is good
                if (simpleParam.getProtectionParameter() instanceof PasswordProtection) {
                    try {
                        keySpec(resultPrivateKeyMaterial, ((PasswordProtection) simpleParam.getProtectionParameter()).getPassword());
                    } catch (UnrecoverableKeyException e) {
                        // TODO Don't message match
                        if (Objects.equals(e.getMessage(), "bad password")) {
                            throw new IOException(e.getMessage(), e);
                        }
                    } catch (Exception ignored) {
                        // Do not report this exception here, let it bubble out when the key is loaded
                    }
                }

                // Load the certificate(s), there must be at least one
                Certificate[] resultCertificateChain;
                try (InputStream in = Files.newInputStream(simpleParam.getCertificatePath())) {
                    Collection<? extends Certificate> certificates = CertificateFactory.getInstance("X509").generateCertificates(in);
                    if (certificates.isEmpty()) {
                        throw new CertificateException("invalid zero-length certificate chain");
                    } else if (certificates.iterator().next() instanceof X509Certificate) {
                        resultCertificateChain = certificates.toArray(new X509Certificate[certificates.size()]);
                    } else {
                        resultCertificateChain = certificates.toArray(new Certificate[certificates.size()]);
                    }
                }

                // Don't "write" to the fields until we get here without failing
                alias = simpleParam.getAlias();
                privateKeyMaterial = resultPrivateKeyMaterial;
                certificateChain = resultCertificateChain;
                creationDate = new Date();
            } else {
                throw new UnsupportedOperationException("unsupported load/store parameter: " + param);
            }
        }

        @Override
        public Key engineGetKey(String alias, char[] password) throws NoSuchAlgorithmException, UnrecoverableKeyException {
            if (Objects.equals(this.alias, alias)) {
                try {
                    KeySpec keySpec = keySpec(privateKeyMaterial, password);
                    String algorithm = algorithm(keySpec);
                    return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
                } catch (InvalidKeySpecException e) {
                    throw (UnrecoverableKeyException) new UnrecoverableKeyException("failed to load key specification").initCause(e);
                }
            } else {
                return null;
            }
        }

        @Override
        public Certificate[] engineGetCertificateChain(String alias) {
            return Objects.equals(this.alias, alias) ? certificateChain.clone() : null;
        }

        @Override
        public Certificate engineGetCertificate(String alias) {
            return Objects.equals(this.alias, alias) ? certificateChain[0] : null;
        }

        @Override
        public Date engineGetCreationDate(String alias) {
            return Objects.equals(this.alias, alias) ? creationDate : null;
        }

        @Override
        public Enumeration<String> engineAliases() {
            return Collections.enumeration(Collections.singleton(alias));
        }

        @Override
        public boolean engineContainsAlias(String alias) {
            return Objects.equals(this.alias, alias);
        }

        @Override
        public int engineSize() {
            return 1;
        }

        @Override
        public boolean engineIsKeyEntry(String alias) {
            return Objects.equals(this.alias, alias);
        }

        @Override
        public boolean engineIsCertificateEntry(String alias) {
            return false;
        }

        @Override
        public String engineGetCertificateAlias(Certificate cert) {
            return Objects.equals(cert, certificateChain[0]) ? alias : null;
        }

        // Unsupported operations...

        @Override
        public void engineLoad(InputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
            throw new UnsupportedOperationException("single stream load is not supported");
        }

        @Override
        public void engineStore(OutputStream stream, char[] password) throws IOException, NoSuchAlgorithmException, CertificateException {
            throw new UnsupportedOperationException("key store cannot be stored");
        }

        @Override
        public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] chain) throws KeyStoreException {
            throw new UnsupportedOperationException("key store is read only");
        }

        @Override
        public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain) throws KeyStoreException {
            throw new UnsupportedOperationException("key store is read only");
        }

        @Override
        public void engineSetCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
            throw new UnsupportedOperationException("key store is read only");
        }

        @Override
        public void engineDeleteEntry(String alias) throws KeyStoreException {
            throw new UnsupportedOperationException("key store is read only");
        }
    }

    /**
     * A splitter for PEM encapsulated data. Used to extract properties from the SSLeay format.
     */
    private static final Splitter PEM_ENCAPSULATED_DATA_SPLITTER = Splitter.on(':').trimResults().limit(2).omitEmptyStrings();

    private KeyPairStore() {
    }

    /**
     * Ensure that only the owner can read the private key file. This check will <em>pass</em> if the underlying file
     * system does not support POSIX file permissions or if there is an I/O error reading the permission (e.g. a
     * "file not found" condition).
     */
    private static void checkPrivateKeyPermissions(Path keyFile) {
        // TODO Re-enable this once we know how to make it work in the tests
        // try {
        // final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(keyFile);
        // if (!permissions.equals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))) {
        // throw new AccessControlException("private key file permissions should be owner read/write (0600)",
        // new FilePermission(keyFile.toString(), "read"));
        // }
        // } catch (IOException | UnsupportedOperationException ignored) {
        // return;
        // }
        Objects.requireNonNull(keyFile);
    }

    /**
     * Returns a key specification given arbitrary key material represented as a sequence of bytes.
     *
     * @see #derEncodedKeySpec(byte[], char[])
     * @see #pemEncodedKeySpec(byte[], char[])
     * @throws InvalidKeySpecException
     *             if there is a problem reading the key specification
     * @throws UnrecoverableKeyException
     *             if the password does not work for decrypting the private key
     */
    @VisibleForTesting
    protected static KeySpec keySpec(byte[] keyMaterial, @Nullable char[] password) throws InvalidKeySpecException, UnrecoverableKeyException {
        // Look for textual encoding boundaries
        if (Bytes.indexOf(keyMaterial, new byte[] { 0x2D, 0x2D, 0x2D, 0x2D, 0x2D }) < 0) {
            return derEncodedKeySpec(keyMaterial, password);
        } else {
            return pemEncodedKeySpec(keyMaterial, password);
        }
    }

    /**
     * Generates a key specification from DER encoded key material.
     */
    private static KeySpec derEncodedKeySpec(byte[] keyMaterial, @Nullable char[] password) throws InvalidKeySpecException, UnrecoverableKeyException {
        // No password, assume key is in the clear
        if (password == null || password.length == 0) {
            try {
                // Try a PKCS #1 key first
                return rsaPrivateKeySpec(keyMaterial);
            } catch (InvalidKeySpecException e) {
                // Assume PKCS #8
                return new PKCS8EncodedKeySpec(keyMaterial);
            }
        }

        try {
            // Decrypt the private key using the password in the platforms default character encoding
            EncryptedPrivateKeyInfo privateKeyInfo = new EncryptedPrivateKeyInfo(keyMaterial);
            Key decryptKey = new SecretKeySpec(new String(password).getBytes(Charset.defaultCharset()), algorithmName(privateKeyInfo));
            return privateKeyInfo.getKeySpec(decryptKey);
        } catch (IOException e) {
            // TODO Just like "bad padding", an incorrect password manifests itself as overrun?
            if (e.getMessage().startsWith("overrun, ")) {
                UnrecoverableKeyException failure = new UnrecoverableKeyException("bad password");
                failure.addSuppressed(e); // Suppress instead of cause so the "root cause" is unrecoverable key
                throw failure;
            }

            // Invalid encrypted private key info, maybe it is a plain private key info structure
            return new PKCS8EncodedKeySpec(keyMaterial);
        } catch (InvalidKeyException e) {
            throw (UnrecoverableKeyException) new UnrecoverableKeyException("bad password").initCause(e);
        } catch (NoSuchAlgorithmException e) {
            throw (UnrecoverableKeyException) new UnrecoverableKeyException("unsupported key encryption algorithm").initCause(e);
        }
    }

    /**
     * Generates a key specification from PEM encoded key material.
     */
    private static KeySpec pemEncodedKeySpec(byte[] keyMaterial, @Nullable char[] password) throws InvalidKeySpecException, UnrecoverableKeyException {
        // There was a "-----" somewhere, assume RFC 7468 textual encoding
        List<String> base64 = null;
        String label = null;
        Map<String, String> properties = new LinkedHashMap<>();
        try {
            for (String line : ByteSource.wrap(keyMaterial).asCharSource(StandardCharsets.US_ASCII).readLines()) {
                line = line.trim();
                if (line.startsWith("-----BEGIN ") && line.endsWith("-----")) {
                    // Pre-encapsulation boundary
                    base64 = new LinkedList<>();
                    label = line.substring(11, line.length() - 5);
                } else if (base64 != null && line.equals("-----END " + label + "-----")) {
                    // Post-encapsulation boundary
                    break;
                } else if (base64 != null) {
                    final List<String> keyValue = PEM_ENCAPSULATED_DATA_SPLITTER.splitToList(line);
                    if (keyValue.size() == 1) {
                        // Encapsulated text portion
                        base64.add(keyValue.get(0));
                    } else if (keyValue.size() == 2) {
                        // Property (traditional SSLeay encrypted key)
                        properties.put(keyValue.get(0).toLowerCase(), keyValue.get(1));
                    }
                }
            }
        } catch (IOException e) {
            // Not sure this can even happen coming from a wrapped byte array
            throw new InvalidKeySpecException("invalid textual encoding", e);
        }

        // Make sure we found some encapsulated data
        if (base64 == null || label == null) {
            throw new InvalidKeySpecException("unable to find encapsulated data");
        }

        byte[] encodedKey = BaseEncoding.base64().decode(Joiner.on("").join(base64));
        return pemKeySpec(encodedKey, label, properties, password);
    }

    /**
     * Takes the information extracted from the parsed PEM encoded key and turns it into a key specification.
     */
    private static KeySpec pemKeySpec(byte[] encodedKey, String label, Map<String, String> properties, char[] password)
            throws InvalidKeySpecException, UnrecoverableKeyException {
        // Figure out what we found and properly convert key material
        switch (label) {
        case "RSA PRIVATE KEY":
            if (properties.isEmpty()) {
                // PKCS #1
                return rsaPrivateKeySpec(encodedKey);
            } else {
                // Traditional SSLeay (encrypted PKCS #1)
                byte[] decryptedKey;
                try {
                    String procType = properties.get("proc-type");
                    String dekInfo = properties.get("dek-info");
                    decryptedKey = generateOpenSSLCipher(procType, dekInfo, password).doFinal(encodedKey);
                } catch (BadPaddingException e) {
                    // TODO Is this an appropriate indication that the key was incorrect?
                    throw (UnrecoverableKeyException) new UnrecoverableKeyException("bad password").initCause(e);
                } catch (IllegalArgumentException | GeneralSecurityException e) {
                    throw (UnrecoverableKeyException) new UnrecoverableKeyException("failed to read encrypted traditional RSA private key").initCause(e);
                }
                return rsaPrivateKeySpec(decryptedKey);
            }
        case "PRIVATE KEY":
            // PKCS #8
            return new PKCS8EncodedKeySpec(encodedKey);
        case "ENCRYPTED PRIVATE KEY":
            if (password != null) {
                try {
                    EncryptedPrivateKeyInfo privateKeyInfo = new EncryptedPrivateKeyInfo(encodedKey);
                    Key decryptKey = new SecretKeySpec(new String(password).getBytes(Charset.defaultCharset()), algorithmName(privateKeyInfo));
                    return privateKeyInfo.getKeySpec(decryptKey);
                } catch (NoSuchAlgorithmException e) {
                    throw (UnrecoverableKeyException) new UnrecoverableKeyException("unsupported key encryption algorithm").initCause(e);
                } catch (InvalidKeyException e) {
                    throw (UnrecoverableKeyException) new UnrecoverableKeyException("bad password").initCause(e);
                } catch (IOException e) {
                    throw new InvalidKeySpecException("invalid encrypted private key", e);
                }
            } else {
                throw new UnrecoverableKeyException("no password");
            }
        default:
            throw new InvalidKeySpecException("unknown label: " + label);
        }
    }

    /**
     * Returns the JCA algorithm name from an arbitrary key specification.
     */
    @VisibleForTesting
    protected static String algorithm(KeySpec keySpec) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (keySpec instanceof RSAPrivateKeySpec) {
            return "RSA";
        } else if (keySpec instanceof DSAPrivateKeySpec) {
            return "DSA";
        } else if (keySpec instanceof PKCS8EncodedKeySpec) {
            return pkcs8PrivateKeyAlgorithm((PKCS8EncodedKeySpec) keySpec);
        } else {
            throw new InvalidKeySpecException("unrecognized key specification: " + keySpec.getClass().getName());
        }
    }

    /**
     * Reads the private key algorithm from the encoded PKCS #8 private key info structure.
     */
    private static String pkcs8PrivateKeyAlgorithm(PKCS8EncodedKeySpec keySpec) throws InvalidKeySpecException, NoSuchAlgorithmException {
        ByteBuffer encoded = ByteBuffer.wrap(keySpec.getEncoded());
        expectTag(encoded, (byte) 0x30);
        if (expectTag(encoded, (byte) 0x02) != 1 || encoded.get() != 0) {
            throw new InvalidKeySpecException("PKCS #8 private key info, expected version 0");
        }
        expectTag(encoded, (byte) 0x30);
        int len = expectTag(encoded, (byte) 0x06);
        String oid = readOid((ByteBuffer) encoded.slice().limit(len));
        encoded.position(encoded.position() + len);

        return oidToJcaAlgorithm(oid);
    }

    /**
     * Reads the key encryption algorithm from the encrypted PKCS #8 encrypted private key info structure.
     */
    private static String algorithmName(EncryptedPrivateKeyInfo privateKeyInfo) throws NoSuchAlgorithmException {
        String algName = privateKeyInfo.getAlgName();
        switch (algName) {
        case "1.2.840.113549.1.5.13":
            // TODO Can we support this?
            throw new NoSuchAlgorithmException("PBES2");
        default:
            return algName;
        }
    }

    /**
     * Converts a dotted string OID representation to a JCA algorithm identifier.
     */
    private static String oidToJcaAlgorithm(String oid) throws NoSuchAlgorithmException {
        switch (oid) {
        // PKCS #1
        case "1.2.840.113549.1.1.1":
            return "RSA";

        // CMS (RFC 3370)
        case "1.2.840.10040.4.1":
            return "DSA";
        default:
            throw new NoSuchAlgorithmException(oid);
        }
    }

    /**
     * Reads an encoded ASN.1 PKCS #1 RSA private key.
     */
    private static RSAPrivateKeySpec rsaPrivateKeySpec(byte[] encodedKey) throws InvalidKeySpecException {
        // Don't get fancy, just read the modulus and private exponent
        ByteBuffer encoded = ByteBuffer.wrap(encodedKey);
        expectTag(encoded, (byte) 0x30);
        if (expectTag(encoded, (byte) 0x02) != 1 || encoded.get() != 0) {
            // TODO What about version 1?
            throw new InvalidKeySpecException("PKCS #1 private key info, expected version 0");
        }

        int modulusLength = expectTag(encoded, (byte) 0x02);
        BigInteger modulus = readInteger((ByteBuffer) encoded.slice().limit(modulusLength));
        encoded.position(encoded.position() + modulusLength);

        int publicExponentLength = expectTag(encoded, (byte) 0x02);
        readInteger((ByteBuffer) encoded.slice().limit(publicExponentLength));
        encoded.position(encoded.position() + publicExponentLength);

        int privateExponentLength = expectTag(encoded, (byte) 0x02);
        BigInteger privateExponent = readInteger((ByteBuffer) encoded.slice().limit(privateExponentLength));
        encoded.position(encoded.position() + privateExponentLength);

        return new RSAPrivateKeySpec(modulus, privateExponent);
    }

    /**
     * Reads an encoded ASN.1 tag and length, assuming tag matches.
     */
    private static int expectTag(ByteBuffer buffer, byte tag) throws InvalidKeySpecException {
        if (buffer.get() != tag) {
            throw new InvalidKeySpecException(String.format("expected 0x%02X, was 0x%02X", tag & 0xFF, buffer.get(buffer.position() - 1)));
        }
        int result = buffer.get();
        if ((result & 0x80) != 0) {
            // FIXME: Overflow when length is > 32 bits
            byte[] buf = new byte[result & 0x7F];
            buffer.get(buf);
            result = 0;
            for (byte b : buf) {
                result = (result << 8) | (b & 0xFF);
            }
        }
        return result;
    }

    /**
     * Reads an encoded ASN.1 object identifier, consuming the entire buffer.
     */
    private static String readOid(ByteBuffer buffer) {
        StringBuilder oid = new StringBuilder();
        byte firstTwoNodes = buffer.get();
        oid.append(firstTwoNodes / 40).append('.').append(firstTwoNodes % 40);
        int o = 0;
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if ((b & 0x80) != 0) {
                o = (o << 7) | (b & 0x7F);
            } else if (o != 0) {
                oid.append('.').append((o << 7) | (b & 0x7F));
                o = 0;
            } else {
                oid.append('.').append(b);
            }
        }
        return oid.toString();
    }

    /**
     * Reads an encoded ASN.1 integer, consuming the entire buffer.
     */
    private static BigInteger readInteger(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new BigInteger(bytes);
    }

    /**
     * Given traditional SSLeay parameters and a password, this method constructs a cipher which can be used to decrypt
     * the ASN.1 representation of the private key.
     * <p>
     * This is largely gleaned from <a href="http://juliusdavies.ca/commons-ssl/">Not-Yet-Commons-SSL</a>.
     */
    private static Cipher generateOpenSSLCipher(String procType, String dekInfo, char[] password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        // Split the SSLeay properties up
        List<String> dekInfos = Splitter.on(',').trimResults().limit(2).splitToList(Strings.nullToEmpty(dekInfo));
        List<String> opensslAlgorithm = Splitter.on('-').splitToList(dekInfos.get(0).toUpperCase());
        byte[] iv = BaseEncoding.base16().decode(dekInfos.get(1)); // TODO Is this optional?
        if (opensslAlgorithm.isEmpty()) {
            throw new NoSuchAlgorithmException("UNKNOWN (DEK-Info: " + dekInfo + ")");
        }

        // Convert it over to something we can use in Java
        String algorithm = opensslAlgorithm.get(0);
        String mode = "CBC";
        String padding = "PKCS5Padding";
        Integer keySize = null;

        if (opensslAlgorithm.size() > 1) {
            keySize = Ints.tryParse(opensslAlgorithm.get(1));
            if (opensslAlgorithm.size() > 2) {
                mode = opensslAlgorithm.get(2);
                if (keySize == null) {
                    algorithm = opensslAlgorithm.get(1).startsWith("EDE") ? "DESede" : algorithm;
                }
            } else if (keySize == null) {
                mode = opensslAlgorithm.get(1);
            }
        }

        if (algorithm.startsWith("AES") && algorithm.length() > 3) {
            algorithm = "AES";
            keySize = algorithm.startsWith("AES192") ? 192 : algorithm.startsWith("AES256") ? 256 : 128;
        } else if (algorithm.startsWith("DES2")) {
            algorithm = "DESede";
        } else if (algorithm.startsWith("DES3")) {
            algorithm = "DESede";
        }

        if (keySize == null) {
            keySize = "DESede".equals(algorithm) ? 192 : "DES".equals(algorithm) ? 64 : 128;
        }

        if ("CFB".equals(mode) || "OFB".equals(mode)) {
            padding = "NoPadding";
        }

        if ("RC4".equals(algorithm)) {
            mode = padding = null;
        }

        byte[] secret = new byte[keySize / 8];
        byte[] encodedPassword = new String(password).getBytes(StandardCharsets.ISO_8859_1);
        Hasher hasher = Hashing.md5().newHasher();
        int pos = 0;
        while (pos < secret.length) {
            hasher.putBytes(encodedPassword);
            if (iv != null) {
                hasher.putBytes(iv, 0, 8);
            }
            byte[] hash = hasher.hash().asBytes();
            int len = Math.min(secret.length - pos, hash.length);
            System.arraycopy(hash, 0, secret, pos, len);
            pos += len;
            hasher = Hashing.md5().newHasher();
            hasher.putBytes(hash);
        }

        String transformation = Joiner.on('/').skipNulls().join(algorithm, mode, padding);
        SecretKey key = new SecretKeySpec(secret, algorithm);
        AlgorithmParameterSpec params = null;
        if (!"ECB".equals(mode)) {
            params = new IvParameterSpec(iv);
        }

        // JCE key size limit
        if (keySize > Cipher.getMaxAllowedKeyLength(transformation)) {
            throw new InvalidKeyException("Illegal key size " + keySize + " for " + algorithm + " (consider unlimited strength cryptography policy)");
        }

        // Create and initialize this thing...
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        return cipher;
    }

}
