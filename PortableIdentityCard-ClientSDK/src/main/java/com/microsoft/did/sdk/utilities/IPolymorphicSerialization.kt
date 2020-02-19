package com.microsoft.did.sdk.utilities

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.collections.Map
import kotlin.reflect.KClass

interface IPolymorphicSerialization {
    fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T
    fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String
    fun <K : Any, V: Any> stringify(obj: Map<K, V>, keyclass: KClass<K>, valclass: KClass<V>): String
    fun <K : Any, V: Any> parseMap(map: String, keyclass: KClass<K>, valclass: KClass<V>): Map<K, V>
    fun <T : Any> stringify(objects: List<T>, keyclass: KClass<T>): String
}