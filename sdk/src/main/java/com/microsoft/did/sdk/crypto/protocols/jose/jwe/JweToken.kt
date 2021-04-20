package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoHelper
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEDecrypter
import com.nimbusds.jose.JWEEncrypter
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDHEncrypter
import com.nimbusds.jose.crypto.PasswordBasedEncrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.crypto.X25519Encrypter
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyConverter
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.RSAKey
import java.security.Key

class JweToken private constructor(
    private var jweToken: JWEObject
) {

    val contentType: String?
        get() = jweToken.header.contentType

    companion object {
        private const val SALT_LENGTH = 8
        private const val ITERATION_COUNT = 100 * 1000

        fun deserialize(jwe: String): JweToken {
            return JweToken(JWEObject.parse(jwe))
        }
    }

    val contentAsByteArray: ByteArray
        get() = this.jweToken.payload.toBytes()
    val contentAsString: String
        get() = this.jweToken.payload.toString()

    constructor(
        plaintext: String, algorithm: JWEAlgorithm = JWEAlgorithm.ECDH_ES_A256KW,
        encryption: EncryptionMethod = EncryptionMethod.A256CBC_HS512
    ) : this(
        JWEObject(
            JWEHeader(algorithm, encryption),
            Payload(plaintext)
        )
    ) {
    }

    fun getKeyAlgorithm(): JWEAlgorithm {
        return jweToken.header.algorithm
    }

    fun encrypt(publicKey: JWK) {
        val encrypter = getEncrypter(publicKey)
        val builder = JWEHeader.Builder(jweToken.header)
            .contentType(contentType)
            .keyID(publicKey.keyID)
            .build()
        jweToken = JWEObject(builder, jweToken.payload)
        jweToken.encrypt(encrypter)
    }

    private fun getEncrypter(publicKey: JWK): JWEEncrypter {
        return when (publicKey) {
            is ECKey -> ECDHEncrypter(publicKey)
            is RSAKey -> RSAEncrypter(publicKey)
            is OctetKeyPair -> X25519Encrypter(publicKey)
            is OctetSequenceKey -> PasswordBasedEncrypter((publicKey).keyValue.decode(), SALT_LENGTH, ITERATION_COUNT)
            else -> throw AlgorithmException("Unknown public key type ${publicKey::class.qualifiedName}")
        }
    }

    fun serialize(): String {
        return jweToken.serialize()
    }

    fun decrypt(keyStore: EncryptedKeyStore? = null, privateKey: Key? = null): ByteArray {
        // we're already decrypted
        if (jweToken.state == JWEObject.State.DECRYPTED || jweToken.state == JWEObject.State.UNENCRYPTED) {
            return jweToken.payload.toBytes()
        }

        val decrypter = getDecrypter(keyStore, privateKey)
        jweToken.decrypt(decrypter)
        return jweToken.payload.toBytes()
    }

    private fun getDecrypter(keyStore: EncryptedKeyStore?, privateKey: Key?): JWEDecrypter {
        if (keyStore == null && privateKey == null) {
            throw IllegalArgumentException("keyStore or privateKey must be passed as input")
        }
        return privateKey?.let {
            getDecrypterByKey(it)
        } ?: keyStore?.let {
            getDecrypterByKeyStore(it)
        } ?: throw KeyException("No key found")
    }

    private fun getDecrypterByKey(privateKey: Key): JWEDecrypter? {
        return try {
            DefaultJWEDecrypterFactory().createJWEDecrypter(jweToken.header, privateKey)
        } catch (exception: JOSEException) {
            null
        }
    }

    private fun getDecrypterByKeyStore(keyStore: EncryptedKeyStore): JWEDecrypter? {
        return jweToken.header.keyID?.let { keyId ->
            val keyRef = JwaCryptoHelper.extractDidAndKeyId(keyId).second
            val key = keyStore.getKey(keyRef)
            // KeyConverter.toJavaKeys exports a public and private key if possible (private key after first)
            DefaultJWEDecrypterFactory().createJWEDecrypter(jweToken.header, KeyConverter.toJavaKeys(listOf(key)).last())
        }
    }
}
