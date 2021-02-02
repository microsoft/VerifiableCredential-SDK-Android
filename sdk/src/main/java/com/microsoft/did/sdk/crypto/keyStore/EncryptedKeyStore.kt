// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import org.spongycastle.asn1.x509.SubjectKeyIdentifier
import org.spongycastle.x509.X509V3CertificateGenerator
import java.io.File
import java.math.BigInteger
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.security.cert.Certificate
import java.util.Date
import javax.crypto.SecretKey
import javax.security.auth.x500.X500Principal

object EncryptedKeyStore {

    private const val KEYSTORE_FILENAME = "didKeyStore.jks"
    private const val FQDM = "self-signed.local"

    enum class KeyPurpose(val value: Int) {
        SIGN(KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY),
        ENCRYPT(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
    }

    private lateinit var encryptedFile: EncryptedFile

    fun initialize(context: Context) {
        encryptedFile = getEncryptedFile(context)
    }

    /**
     * DO NOT add or modify entries of this keyStore directly, instead use the `store...`
     * functions of this class otherwise keys may not be persisted.
     */
    val keyStore by lazy { loadKeyStore() }

    @Suppress("UNCHECKED_CAST")
    fun <T : Key> getKey(keyId: String): T {
        return keyStore.getKey(keyId, null) as? T
            ?: throw KeyStoreException("Stored key $keyId is not of the requested Key type")
    }

    fun storeKeyPair(keyPair: KeyPair, keyId: String, purpose: KeyPurpose = KeyPurpose.SIGN) {
        val certChain = createSelfSignedCertificateChain(keyPair, keyId)
        val protection = KeyProtection.Builder(purpose.value).build()

        keyStore.setEntry(
            keyId,
            KeyStore.PrivateKeyEntry(keyPair.private, certChain),
            protection
        )
        saveToFile(keyStore)
    }

    fun storeSecretKey(secretKey: SecretKey, keyId: String) {
        val protection = KeyProtection.Builder(KeyPurpose.ENCRYPT.value).build()
        keyStore.setEntry(keyId, KeyStore.SecretKeyEntry(secretKey), protection)
        saveToFile(keyStore)
    }

    private fun saveToFile(keyStore: KeyStore) {
        keyStore.store(encryptedFile.openFileOutput(), null)
    }

    private fun createSelfSignedCertificateChain(keyPair: KeyPair, keyId: String): Array<Certificate> {
        return arrayOf(createSelfSignedCertificate(keyPair, keyId))
    }

    private fun createSelfSignedCertificate(keyPair: KeyPair, keyId: String): Certificate {
        val owner = X500Principal("CN=$FQDM")
        val randomBytes = ByteArray(64)
        SecureRandom().nextBytes(randomBytes)
        val builder = X509V3CertificateGenerator()
        builder.setSerialNumber(BigInteger(randomBytes))
        builder.setIssuerDN(owner)
        builder.setNotBefore(Date())
        builder.setNotAfter(Date())
        builder.setSubjectDN(owner)
        builder.setPublicKey(keyPair.public)
        builder.setSignatureAlgorithm("SHA256WITHPLAIN-ECDSA")
        builder.addExtension(
            org.spongycastle.asn1.x509.X509Extension.subjectKeyIdentifier, false,
            SubjectKeyIdentifier(keyId.toByteArray())
        )
        return builder.generate(keyPair.private, "SC")
    }

    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(encryptedFile.openFileInput(), null)
        return keyStore
    }

    private fun getEncryptedFile(context: Context): EncryptedFile {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedFile.Builder(
            File(context.filesDir, KEYSTORE_FILENAME),
            context, masterKeyAlias, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }
}