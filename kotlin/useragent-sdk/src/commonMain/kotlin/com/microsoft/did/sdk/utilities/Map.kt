package com.microsoft.did.sdk.utilities

import kotlin.collections.Map

object Map {
    fun <X, Y> join(vararg maps: Map<X, Y>): Map<X, Y> {
        val result = mutableMapOf<X, Y>()
        maps.forEach {
            result.putAll(it)
        }
        return result
    }
}