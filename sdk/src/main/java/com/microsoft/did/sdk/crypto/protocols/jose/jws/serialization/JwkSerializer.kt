// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.protocols.jose.jws.serialization

import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.RSAKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

/**
 * This object tells the Kotlin Serialization framework how to serialize a JWK.
 * Because JWK is a java class from a third-party provider a so called "surrogate"
 * class is required. This class is not for use elsewhere.
 *
 * Please refer to kotlin serialization docs for more info:
 * https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#composite-serializer-via-surrogate
 */
object JwkSerializer : KSerializer<JWK> {
    override val descriptor: SerialDescriptor = JwkSurrogate.serializer().descriptor
    override fun serialize(encoder: Encoder, value: JWK) {
        val ecJwk = value as? ECKey
        val rsaJwk = value as? RSAKey
        val octJwk = value as? OctetSequenceKey
        val okpJwk = value as? OctetKeyPair
        encoder.encodeSerializableValue(
            JwkSurrogate.serializer(), JwkSurrogate(
                kty = value.keyType.value,
                kid = value.keyID,
                use = value.keyUse?.value,
                key_ops = value.keyOperations?.toList()?.map { it.identifier() },
                alg = value.algorithm?.name,
                crv = ecJwk?.curve?.stdName ?: okpJwk?.curve?.stdName,
                x = ecJwk?.x?.toString() ?: okpJwk?.x?.toString(),
                y = ecJwk?.y?.toString(),
                d = ecJwk?.d?.toString() ?: rsaJwk?.privateExponent?.toString() ?: okpJwk?.d?.toString(),
                n = rsaJwk?.modulus?.toString(),
                e = rsaJwk?.publicExponent?.toString(),
                p = rsaJwk?.firstPrimeFactor?.toString(),
                q = rsaJwk?.secondPrimeFactor?.toString(),
                dp = rsaJwk?.firstFactorCRTExponent?.toString(),
                dq = rsaJwk?.secondFactorCRTExponent?.toString(),
                qi = rsaJwk?.firstCRTCoefficient?.toString(),
                oth = rsaJwk?.otherPrimes?.map {
                    JwkSurrogate.RsaOtherPrimesInfo(
                        r = it.primeFactor.toString(),
                        d = it.factorCRTExponent.toString(),
                        t = it.factorCRTCoefficient.toString()
                    )
                },
                k = octJwk?.keyValue?.toString()
            )
        )
    }

    override fun deserialize(decoder: Decoder): JWK {
        val jsonWebKey = decoder.decodeSerializableValue(JwkSurrogate.serializer())
        return JWK.parse(Json.encodeToString(JwkSurrogate.serializer(), jsonWebKey))
    }

    /**
     * This class is not supposed to be used anywhere else. Use Nimbus JWK instead. See JwkSerializer class comment.
     */
    @Serializable
    private data class JwkSurrogate(
        // The following fields are defined in Section 3.1 of JSON Web Key
        var kty: String = "",
        var kid: String? = null,
        var use: String? = null,
        var key_ops: List<String>? = null,
        var alg: String? = null,

        // The following fields are defined in JSON Web Key Parameters Registration
        var ext: Boolean? = null,

        // The following fields are defined in Section 6 of JSON Web Algorithms
        var crv: String? = null,
        var x: String? = null,
        var y: String? = null,
        var d: String? = null,
        var n: String? = null,
        var e: String? = null,
        var p: String? = null,
        var q: String? = null,
        var dp: String? = null,
        var dq: String? = null,
        var qi: String? = null,
        var oth: List<RsaOtherPrimesInfo>? = null,
        var k: String? = null
    ) {
        /** The following fields are defined in Section 6.3.2.7 of JSON Web Algorithms */
        @Serializable
        data class RsaOtherPrimesInfo(val r: String, val d: String, val t: String)
    }
}