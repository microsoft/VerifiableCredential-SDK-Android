// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.utilities.serializer

import com.microsoft.did.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.did.sdk.auth.models.serviceResponses.PresentationServiceResponse
import com.microsoft.did.sdk.auth.models.serviceResponses.ServiceResponse
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.collections.Map

@Singleton
class Serializer @Inject constructor() : ISerializer {
    private val serviceResponseSerializer = SerializersModule {
        polymorphic(ServiceResponse::class) {
            IssuanceServiceResponse::class with IssuanceServiceResponse.serializer()
            PresentationServiceResponse::class with PresentationServiceResponse.serializer()
        }
    }

    val json: Json = Json(
        context = serviceResponseSerializer,
        configuration = JsonConfiguration(
            encodeDefaults = false,
            strictMode = false
        )
    )

    override fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T =
        json.parse(deserializer, string)

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String =
        json.stringify(serializer, obj)

    fun <K : Any, V : Any> stringify(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String {
        val serializer: ISerializer = this
        return serializer.stringifyImpl(obj, keyClass, valClass)
    }

    @ImplicitReflectionSerializer
    override fun <K : Any, V : Any> stringifyImpl(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String {
        return json.stringify((keyClass.serializer() to valClass.serializer()).map, obj)
    }

    fun <K : Any, V : Any> parseMap(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V> {
        val serializer: ISerializer = this
        return serializer.parseMapImpl(map, keyClass, valClass)
    }

    @ImplicitReflectionSerializer
    override fun <K : Any, V : Any> parseMapImpl(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V> {
        return parse((keyClass.serializer() to valClass.serializer()).map, map)
    }

    fun <T : Any> stringify(objects: List<T>, keyClass: KClass<T>): String {
        val serializer: ISerializer = this
        return serializer.stringifyImpl(objects, keyClass)
    }

    @ImplicitReflectionSerializer
    override fun <T : Any> stringifyImpl(objects: List<T>, keyClass: KClass<T>): String {
        return stringify((keyClass.serializer()).list, objects)
    }

}