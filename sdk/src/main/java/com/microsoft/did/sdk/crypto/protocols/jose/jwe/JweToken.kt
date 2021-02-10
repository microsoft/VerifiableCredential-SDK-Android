package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoHelper
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
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

const val SALT_LENGTH = 8
const val ITERATION_COUNT = 100 * 1000

class JweToken private constructor (
    private val jweToken: JWEObject
) {
    companion object {
        fun deserialize(jwe: String): JweToken {
            return JweToken(JWEObject.parse(jwe))
        }
    }

    val contentAsByteArray: ByteArray
        get() = this.jweToken.payload.toBytes()
    val contentAsString: String
        get() = this.jweToken.payload.toString()

    var contentType: String? = jweToken.header.contentType

    constructor(plaintext: String, algorithm: JWEAlgorithm = JWEAlgorithm.ECDH_ES_A256KW,
                encryption: EncryptionMethod = EncryptionMethod.A256CBC_HS512): this(
        JWEObject(
            JWEHeader(algorithm, encryption),
            Payload(plaintext)
        )) {}

    fun getKeyAlgorithm(): JWEAlgorithm {
        return jweToken.header.algorithm
    }

    fun encrypt(publicKey: JWK)  {
        var encrypter: JWEEncrypter? = null
        when (publicKey::class) {
            ECKey::class -> {
                encrypter = ECDHEncrypter(publicKey as ECKey)
            }
            RSAKey::class -> {
                encrypter = RSAEncrypter(publicKey as RSAKey)
            }
            OctetKeyPair::class -> {
                encrypter = X25519Encrypter(publicKey as OctetKeyPair)
            }
            OctetSequenceKey::class -> {
                encrypter = PasswordBasedEncrypter((publicKey as OctetSequenceKey).keyValue.decode(), SALT_LENGTH, ITERATION_COUNT)
            }
        }
        encrypter?.let {
            jweToken.encrypt(it)
            return
        }
        throw AlgorithmException("Unsupported JWK")
    }

    fun serialize(): String {
        return jweToken.serialize()
    }

    fun decrypt(keyStore: EncryptedKeyStore? = null, privateKey: Key? = null): ByteArray? {
        // attempt with a specific key
        var decrypter: JWEDecrypter? = null
        if (privateKey != null) {
            try {
                decrypter = DefaultJWEDecrypterFactory().createJWEDecrypter(jweToken.header, privateKey)
            } catch (exception: JOSEException) {
                return null
            }
        } else if (keyStore != null) {
            jweToken.header.keyID?.let { keyId ->
                val keyRef = JwaCryptoHelper.extractDidAndKeyId(keyId).second
                val key = keyStore.getKey(keyRef)
                decrypter = DefaultJWEDecrypterFactory().createJWEDecrypter(jweToken.header, KeyConverter.toJavaKeys(listOf(key)).first())
            }
        } else {
            throw IllegalArgumentException("keyStore or privateKey must be passed as input")
        }
        if (decrypter != null) {
            try {
                jweToken.decrypt(decrypter)
            } catch (exception: JOSEException) {
                return null
            }
            return jweToken.payload.toBytes()
        }
        // no specific key was found
        return null
    }
}
