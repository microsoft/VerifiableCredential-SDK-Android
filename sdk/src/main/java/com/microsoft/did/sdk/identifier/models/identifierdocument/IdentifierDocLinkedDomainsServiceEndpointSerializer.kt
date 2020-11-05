// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.util.controlflow.LinkedDomainEndpointInUnknownFormatException
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
 * This class serializes/deserializes serviceEndpoint property in IdentifierDocument.
 * The serviceEndpoint can be a String or Key/Value pair where key is "origins" and value is a list of String.
 * It only supports service endpoint of type "LinkedDomains".
 * @see [Well Known DID Configuration] (https://identity.foundation/.well-known/resources/did-configuration/#linked-domain-service-endpoint)
 */
@Serializer(forClass = List::class)
class IdentifierDocLinkedDomainsServiceEndpointSerializer(@Suppress("UNUSED_PARAMETER") dataSerializer: KSerializer<String>) :
    KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("serviceEndpoint", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeSerializableValue(ListSerializer(String.serializer()), value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        return when (val serviceEndpointJsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> listOf(serviceEndpointJsonElement.content)
            is JsonObject -> {
                val jsonObjectKey = serviceEndpointJsonElement.keys.find { it.equals(ServiceEndpointKeys.Origins.value, true) }
                if (jsonObjectKey != null) {
                    val jsonArray = serviceEndpointJsonElement[jsonObjectKey] as JsonArray
                    jsonArray.map { jsonElement -> (jsonElement as JsonPrimitive).content }
                } else emptyList()
            }
            else -> throw LinkedDomainEndpointInUnknownFormatException("Linked Domains service endpoint is not in the correct format")
        }
    }
}

enum class ServiceEndpointKeys(val value: String) {
    Origins("origins")
}
