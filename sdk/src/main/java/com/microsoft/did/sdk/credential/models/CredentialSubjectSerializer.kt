// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.models

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonPrimitive

/**
 * We currently only support Strings as values of claims contained under credentialSubject:.
 *
 * This serializer adds support for Arrays and Objects. Internally it will still just be treated
 * as String, but this serializer makes sure to serialize any Array or Object claim to a String.
 *
 * Information about the structure of the original claim is lost (other than inspecting the String itself)
 */
object CredentialSubjectSerializer :
    JsonTransformingSerializer<Map<String, String>>(MapSerializer(String.serializer(), String.serializer())) {

    private val serializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) throw SerializationException("CredentialSubject has to be a JSON object")
        val newContent = HashMap<String, JsonElement>()
        element.entries.forEach { entry ->
            val jsonAsString = serializer.encodeToString(JsonElement.serializer(), entry.value)
            newContent[entry.key] = JsonPrimitive(jsonAsString)
        }
        return JsonObject(newContent)
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) throw SerializationException("CredentialSubject has to be a JSON object")
        val newContent = HashMap<String, JsonElement>()
        element.entries.forEach { entry ->
            newContent[entry.key] = serializer.decodeFromString(JsonElement.serializer(), entry.value.jsonPrimitive.content)
        }
        return JsonObject(newContent)
    }

}