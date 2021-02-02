// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.microsoft.did.sdk.crypto.KeyAlgorithm
import com.microsoft.did.sdk.util.Constants
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.SecureRandom
import java.security.cert.Certificate
import java.util.Date
import javax.crypto.SecretKey

object EncryptedKeyStore {

    private const val KEYSTORE_FILENAME = "didKeyStore.jks"
    private const val FQDM = "self-signed.local"

    private lateinit var encryptedFile: EncryptedFile
    private lateinit var encryptedFileLocation: File

    fun initialize(
        context: Context
    ) {
        initializeEncryptedFile(context)
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

    fun storeKeyPair(keyPair: KeyPair, keyId: String) {
        val certChain = createSelfSignedCertificateChain(keyPair, keyId)

        keyStore.setEntry(
            keyId,
            KeyStore.PrivateKeyEntry(keyPair.private, certChain),
            null
        )
        saveToFile(keyStore)
    }

    fun storeSecretKey(secretKey: SecretKey, keyId: String) {
        keyStore.setEntry(keyId, KeyStore.SecretKeyEntry(secretKey), null)
        saveToFile(keyStore)
    }

    private fun saveToFile(keyStore: KeyStore) {
        encryptedFileLocation.delete()
        keyStore.store(encryptedFile.openFileOutput(), null)
    }

    private fun createSelfSignedCertificateChain(keyPair: KeyPair, keyId: String): Array<Certificate> {
        return arrayOf(createSelfSignedCertificate(keyPair, keyId))
    }

    private fun createSelfSignedCertificate(keyPair: KeyPair, keyId: String, ): Certificate {
        val owner = X500Name("CN=$FQDM");
        val builder = X509v3CertificateBuilder(
            owner,
            BigInteger(64, SecureRandom()),
            Date(),
            Date(),
            owner,
            SubjectPublicKeyInfo(
                ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC),
                keyPair.public.encoded
            )
        )
        builder.addExtension(
            Extension.subjectKeyIdentifier, false,
            SubjectKeyIdentifier(keyId.toByteArray())
        )
        val certHolder = builder.build(JcaContentSignerBuilder("SHA256WithECDSA").build(keyPair.private))
        val cert = JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder)
        cert.verify(keyPair.public)
        return cert
    }

    private fun loadKeyStore(): KeyStore {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        try {
            keyStore.load(encryptedFile.openFileInput(), null)
        } catch (exp: IOException) {
            keyStore.load(null, null)
        }
        return keyStore
    }

    private fun initializeEncryptedFile(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedFileLocation = File(context.filesDir, KEYSTORE_FILENAME)
        encryptedFile = EncryptedFile.Builder(
            encryptedFileLocation,
            context, masterKeyAlias, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }
}