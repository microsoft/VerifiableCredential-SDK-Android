// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * This class serializes/deserializes format property in Presentation Request.
 * The format is a Key/Value pair where key can be "jwt_vp" and value is a list of String.
 * @see [Presentation Exchange] (https://identity.foundation/presentation-exchange/#presentation-submission)
 */
@Serializer(forClass = List::class)
class PresentationRequestFormatSerializer(@Suppress("UNUSED_PARAMETER") dataSerializer: KSerializer<String>) :
    KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("format", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeSerializableValue(ListSerializer(String.serializer()), value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        val algList = mutableListOf<String>()
        val formatJsonElement = (decoder as JsonDecoder).decodeJsonElement()
        val formatJsonObjectKey = (formatJsonElement as JsonObject).keys.find { it.equals(FormatKeys.JwtVp.value, true) }
        if (formatJsonObjectKey != null) {
            val algJsonObject = formatJsonElement[formatJsonObjectKey] as JsonObject
            val algJsonObjectKey = algJsonObject.keys.find { it.equals(AlgorithmKeys.Alg.value, true) }
            if (algJsonObjectKey != null) {
                val jsonArray = algJsonObject[algJsonObjectKey] as JsonArray
                algList.addAll(jsonArray.map { jsonElement -> (jsonElement as JsonPrimitive).content })
            }
        }
        return algList
    }
}

enum class FormatKeys(val value: String) {
    JwtVp("jwt_vp")
}

enum class AlgorithmKeys(val value: String) {
    Alg("alg")
}
