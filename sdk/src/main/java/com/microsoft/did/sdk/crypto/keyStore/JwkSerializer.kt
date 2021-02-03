// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.RSAKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object JwkSerializer : KSerializer<JWK> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JWK", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: JWK) {
        val ecJwk = value as? ECKey
        val rsaJwk = value as? RSAKey
        val octJwk = value as? OctetSequenceKey
        val okpJwk = value as? OctetKeyPair
        encoder.encodeSerializableValue(JsonWebKey.serializer(), JsonWebKey(
            kty = value.keyType.value,
            kid = value.keyID,
            use = value.keyUse?.value,
            key_ops = value.keyOperations?.toList()?.map { it.identifier() },
            alg = value.algorithm?.name,
            crv = ecJwk?.curve?.stdName ?: okpJwk?.curve?.stdName,
            x = ecJwk?.x?.toString() ?: okpJwk?.x?.toString(),
            y = ecJwk?.y?.toString(),
            d = ecJwk?.d?.toString() ?: rsaJwk?.privateExponent?.toString() ?:
                okpJwk?.d?.toString(),
            n = rsaJwk?.modulus?.toString(),
            e = rsaJwk?.publicExponent?.toString(),
            p = rsaJwk?.firstPrimeFactor?.toString(),
            q = rsaJwk?.secondPrimeFactor?.toString(),
            dp = rsaJwk?.firstFactorCRTExponent?.toString(),
            dq = rsaJwk?.secondFactorCRTExponent?.toString(),
            qi = rsaJwk?.firstCRTCoefficient?.toString(),
            oth = rsaJwk?.otherPrimes?.map { JsonWebKey.RsaOtherPrimesInfo(
                r = it.primeFactor.toString(),
                d = it.factorCRTExponent.toString(),
                t = it.factorCRTCoefficient.toString()
            )},
            k = octJwk?.keyValue?.toString()
        ))
    }
    override fun deserialize(decoder: Decoder): JWK {
        val jsonWebKey = decoder.decodeSerializableValue(JsonWebKey.serializer())
        return JWK.parse(Json.encodeToString(JsonWebKey.serializer(), jsonWebKey))
    }
}