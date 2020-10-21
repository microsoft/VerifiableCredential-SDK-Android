// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.LinkedDomainEndpointInUnknownFormatException
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decode
import kotlinx.serialization.encode
import kotlinx.serialization.json.JsonElementSerializer
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content

@Serializer(forClass = List::class)
class IdentifierDocServiceEndpointSerializer (dataSerializer: KSerializer<String> ) : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("serviceEndpoint", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encode(ListSerializer(String.serializer()), value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        return when (val serviceEndpointJsonElement = decoder.decode(JsonElementSerializer)) {
            is JsonLiteral -> listOf(serviceEndpointJsonElement.content)
            is JsonObject -> (serviceEndpointJsonElement.getArray(Constants.LINKED_DOMAINS_SERVICE_ENDPOINT_ORIGINS)).map { it.content }
            else -> throw LinkedDomainEndpointInUnknownFormatException("Linked Domains service endpoint is not in the correct format")
        }
    }
}
