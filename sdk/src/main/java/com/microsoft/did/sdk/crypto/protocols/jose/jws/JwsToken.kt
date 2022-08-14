package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.Ed25519Verifier
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyConverter
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.util.Base64URL
import java.security.PublicKey

class JwsToken constructor(
    private var jwsObject: JWSObject
) {

    val keyId: String?
        get() = jwsObject.header.keyID

    companion object {
        fun deserialize(jws: String): JwsToken {
            return JwsToken(JWSObject.parse(jws))
        }
    }

    constructor(content: ByteArray, jwsAlgorithm: JWSAlgorithm) : this(JWSObject(JWSHeader(jwsAlgorithm), Payload(content)))

    constructor(content: String, jwsAlgorithm: JWSAlgorithm) : this(JWSObject(JWSHeader(jwsAlgorithm), Payload(Base64URL.encode(content))))

    fun serialize(): String {
        return jwsObject.serialize()
    }

    fun sign(privateKey: JWK, overrideHeaders: JWSHeader? = null) {
        overrideHeaders?.let { headers ->
            jwsObject = JWSObject(headers, jwsObject.payload)
        }
        val signer = DefaultJWSSignerFactory().createJWSSigner(privateKey, jwsObject.header.algorithm)
        jwsObject.sign(signer)
    }

    fun verify(publicKeyJWKs: List<JWK> = emptyList()): Boolean {
        for (key in publicKeyJWKs) {
            return if (key.keyType.value == Constants.ED25519_CURVE_KTY) {
                val octetKeyPair = key.toOctetKeyPair()
                verifyUsingOctetKeyPair(octetKeyPair)
            } else {
                val publicKey = KeyConverter.toJavaKeys(listOf(key)).first() as PublicKey
                verifyUsingPublicKey(publicKey)
            }
        }
        return false
    }

    private fun verifyUsingPublicKey(publicKey: PublicKey): Boolean {
        val verifier = DefaultJWSVerifierFactory().createJWSVerifier(jwsObject.header, publicKey)
        if (jwsObject.verify(verifier)) {
            return true
        }
        return false
    }

    private fun verifyUsingOctetKeyPair(octetKeyPair: OctetKeyPair): Boolean {
        val verifier = Ed25519Verifier(octetKeyPair)
        if (jwsObject.verify(verifier)) {
            return true
        }
        return false
    }

    /**
     * Plaintext payload content
     */
    fun content(): String {
        return jwsObject.payload.toString()
    }
}