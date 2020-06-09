package com.microsoft.did.sdk.util

fun stringToByteArray(str: String): ByteArray {
    return (str.map { c -> c.toByte() }).toByteArray()
}

fun byteArrayToString(array: ByteArray): String {
    return String((array.map { b -> b.toChar() }).toCharArray())
}

fun ByteArray.toReadableString(): String {
    val sb = StringBuilder("[")
    this.forEachIndexed { index, it ->
        if (it < 0) sb.append(it + 256) else sb.append(it)
        if (index < this.size) sb.append(", ")
    }
    return sb.append("]").toString()
}