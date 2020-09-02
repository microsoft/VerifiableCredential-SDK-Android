// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util.serializer

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class Serializer @Inject constructor() : ISerializer {
    val json: Json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        isLenient = true

    }

    override fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T =
        json.decodeFromString(deserializer, string)

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String =
        json.encodeToString(serializer, obj)

    fun <K : Any, V : Any> stringify(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String {
        val serializer: ISerializer = this
        return serializer.stringifyImpl(obj, keyClass, valClass)
    }

    @InternalSerializationApi
    override fun <K : Any, V : Any> stringifyImpl(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String {
        return json.encodeToString(MapSerializer(keyClass.serializer(), valClass.serializer()), obj)
    }

    fun <K : Any, V : Any> parseMap(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V> {
        val serializer: ISerializer = this
        return serializer.parseMapImpl(map, keyClass, valClass)
    }

    @InternalSerializationApi
    override fun <K : Any, V : Any> parseMapImpl(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V> {
        return parse(MapSerializer(keyClass.serializer(), valClass.serializer()), map)
    }

    fun <T : Any> stringify(objects: List<T>, keyClass: KClass<T>): String {
        val serializer: ISerializer = this
        return serializer.stringifyImpl(objects, keyClass)
    }

    @InternalSerializationApi
    override fun <T : Any> stringifyImpl(objects: List<T>, keyClass: KClass<T>): String {
        return stringify(ListSerializer((keyClass.serializer())), objects)
    }

}