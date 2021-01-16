package com.microsoft.did.sdk.util


// TODO Replace with default functions
fun stringToByteArray(str: String): ByteArray {
    return (str.map { c -> c.toByte() }).toByteArray()
}

fun byteArrayToString(array: ByteArray): String {
    return String((array.map { b -> b.toChar() }).toCharArray())
}