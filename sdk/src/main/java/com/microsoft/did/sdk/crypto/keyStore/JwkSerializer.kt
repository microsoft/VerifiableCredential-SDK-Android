// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import com.nimbusds.jose.jwk.JWK
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = JWK::class)
object JwkSerializer : KSerializer<JWK> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JWK", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: JWK) = encoder.encodeString(value.toJSONString())
    override fun deserialize(decoder: Decoder): JWK = JWK.parse(decoder.decodeString())
}