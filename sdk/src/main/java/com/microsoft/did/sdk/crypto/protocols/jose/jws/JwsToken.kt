package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.shaded.json.parser.ParseException
import com.nimbusds.jose.util.Base64URL
import java.security.PublicKey

class JwsToken private constructor(
    private var jwsObject: JWSObject
) {
    var keyId: String? = jwsObject.header.keyID
    var type: JOSEObjectType? = jwsObject.header.type

    companion object {
        fun deserialize(jws: String): JwsToken {
            return JwsToken(JWSObject.parse(jws))
        }
    }

    constructor(content: ByteArray) : this(JWSObject(JWSHeader(JWSAlgorithm.ES256K), Payload(content)))

    constructor(content: String) : this(JWSObject(JWSHeader(JWSAlgorithm.ES256K), Payload(Base64URL.encode(content))))

    fun serialize(): String {
        return jwsObject.serialize()
    }

    fun sign(privateKey: JWK) {
        val builder = JWSHeader.Builder(jwsObject.header)
        keyId?.let { builder.keyID(it) }
        type?.let { builder.type(it) }
        jwsObject = JWSObject(builder.build(), jwsObject.payload)
        val signer = DefaultJWSSignerFactory().createJWSSigner(privateKey, jwsObject.header.algorithm)
        jwsObject.sign(signer)
    }

    fun verify(publicKeys: List<PublicKey> = emptyList()): Boolean {
        for (key in publicKeys) {
            val verifier = DefaultJWSVerifierFactory().createJWSVerifier(jwsObject.header, key)
            if (jwsObject.verify(verifier)) {
                return true
            }
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