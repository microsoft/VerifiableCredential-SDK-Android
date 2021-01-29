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
import java.security.KeyPair
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.util.*
import javax.security.auth.x500.X500Principal


object PasswordlessKeyStore {
    private val keyName = "DID_KEYVAULT_LOCK"
    private val keyStoreFileName = "didKeyStore.jks"
    private val fqdn = "self-signed.local"

    public enum class Purpose {
        SIGN,
        ENCRYPT
    }

    fun createKeyStore(context: Context): KeyStore {
        val encryptedFile = getEncryptedFile(context)
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(encryptedFile.openFileInput(), null)
        return keyStore
    }

    fun savePrivateKey(context: Context, keyStore: KeyStore, keyPair: KeyPair, keyId: String, purpose: Purpose) {
        val serverChain: Array<Certificate?> = arrayOfNulls(1)
        val builder = X509V3CertificateGenerator()
        val owner = X500Principal("CN=$fqdn");
        val randomBytes = ByteArray(64)
        SecureRandom().nextBytes(randomBytes)
        builder.setSerialNumber(BigInteger(randomBytes))
        builder.setIssuerDN(owner)
        builder.setNotBefore(Date())
        builder.setNotAfter(Date())
        builder.setSubjectDN(owner)
        builder.setPublicKey(keyPair.public)
        builder.setSignatureAlgorithm("SHA256WithRSAEncryption")
        builder.addExtension(org.spongycastle.asn1.x509.X509Extension.subjectKeyIdentifier, false,
        SubjectKeyIdentifier(keyId.toByteArray()))
        serverChain[0] = builder.generate(keyPair.private, "SC") // note: private key of CA

        var protectionPurpose = when (purpose) {
            Purpose.SIGN -> KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            Purpose.ENCRYPT -> KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_WRAP_KEY
        }
        val protection = KeyProtection.Builder(protectionPurpose)

        keyStore.setEntry(
            keyId,
            KeyStore.PrivateKeyEntry(keyPair.private, serverChain as Array<Certificate>),
            protection.build())
        val encryptedFile = getEncryptedFile(context)
        keyStore.store(encryptedFile.openFileOutput(), null)
    }

    private fun getEncryptedFile(context: Context): EncryptedFile {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedFile.Builder(
            File(context.filesDir, keyStoreFileName),
            context, masterKeyAlias, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build()

    }
}