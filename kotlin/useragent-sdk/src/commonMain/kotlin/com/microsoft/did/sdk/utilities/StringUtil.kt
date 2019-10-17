package com.microsoft.did.sdk.utilities

fun stringToByteArray(str: String): ByteArray {
    return (str.map { c -> c.toByte() }).toByteArray()
}

fun byteArrayToString(array: ByteArray): String {
    return String((array.map { b -> b.toChar() }).toCharArray())
}

fun printBytes(array: ByteArray) {
    print("[")
    array.forEachIndexed{
        index, it ->
        if (it < 0) {
            print(it + 256)
        } else {
            print(it)
        }
        if (index < array.size) {
            print(", ")
        }
    }
    println("]")
}