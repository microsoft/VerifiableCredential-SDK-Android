/*
Kotlin serialization supports polymorphic serialization but it is in experimental stage currently which requires usage
of @ImplicitReflectionSerializer annotation. Any method which uses serializer() as a parameter to determine the class
type requires this annotation. Tried moving to gson library instead of kotlin serialization but the same problem there
as well, polymorphic serialization methods are not exposed but only experimental. Adding this interface to abstract the
serialization methods so that all the nested method calls need not have to annotate but make the method calls via this
interface until another option is explored.
 */
package com.microsoft.did.sdk.utilities

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.collections.Map
import kotlin.reflect.KClass

interface ISerializer {
    fun <T> parse(deserializer: DeserializationStrategy<T>, string: String): T
    fun <T> stringify(serializer: SerializationStrategy<T>, obj: T): String
    fun <K : Any, V: Any> stringifyImpl(obj: Map<K, V>, keyClass: KClass<K>, valClass: KClass<V>): String
    fun <K : Any, V: Any> parseMapImpl(map: String, keyClass: KClass<K>, valClass: KClass<V>): Map<K, V>
    fun <T : Any> stringifyImpl(objects: List<T>, keyClass: KClass<T>): String
}