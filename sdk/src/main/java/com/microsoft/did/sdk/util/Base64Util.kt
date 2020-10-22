package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.util.controlflow.EncodingException
import kotlin.math.max
import kotlin.math.min

object Base64 {
    private val dictionary = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
        "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
        "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "/"
    )
    private const val padding = '='
    fun encode(data: ByteArray): String {
        return encode(data, dictionary, padding)
    }

    fun decode(base64: String): ByteArray {
        return decode(base64, dictionary, padding)
    }
}

object Base64Url {
    private val dictionary = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
        "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
        "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", "_"
    )

    fun encode(data: ByteArray): String {
        return encode(data, dictionary, null)
    }

    fun decode(base64url: String): ByteArray {
        return decode(base64url, dictionary, null)
    }
}

private class ByteGroup private constructor(val ir: List<Int>, val bytes: Int) {

    companion object {
        fun uInt(byte: Byte): Int {
            val value = byte.toInt()
            return if (value < 0) {
                value + 256
            } else {
                value
            }
        }

        fun fromString(data: String, dictionary: List<String>, padding: Char? = null): ByteGroup {
            if (data.length > 4) {
                throw EncodingException("Invalid base64 byte group length")
            }
            val minimalIr = data.filter {
                padding == null || it != padding
            }.map {
                dictionary.indexOf(it.toString())
            }
            return when (minimalIr.size) {
                2 -> {
                    ByteGroup(listOf(minimalIr[0], minimalIr[1], 0, 0), 1)
                }
                3 -> {
                    ByteGroup(listOf(minimalIr[0], minimalIr[1], minimalIr[2], 0), 2)
                }
                4 -> {
                    ByteGroup(minimalIr, 3)
                }
                0, 1 -> {
                    throw EncodingException("Invalid base64 encoding")
                }
                else -> throw EncodingException("Invalid base64 encoding")
            }
        }

        fun fromByteArray(data: ByteArray): ByteGroup {
//        +--first octet--+-second octet--+--third octet--+
//        |7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|
//        +-----------+---+-------+-------+---+-----------+
//        |5 4 3 2 1 0|5 4 3 2 1 0|5 4 3 2 1 0|5 4 3 2 1 0|
//        +--1.index--+--2.index--+--3.index--+--4.index--+
            // bit shifting is only available in Int and Long. Int is 32, capable of holding all bytes.
            var inputGroupInt = uInt(data[0]).shl(16)
            inputGroupInt = inputGroupInt or if (data.size > 1) {
                uInt(data[1]).shl(8)
            } else {
                0
            }
            inputGroupInt = inputGroupInt or if (data.size > 2) {
                uInt(data[2])
            } else {
                0
            }
            val index1 = inputGroupInt.and(0x00fc0000).shr(18)
            val index2 = inputGroupInt.and(0x0003f000).shr(12)
            val index3 = inputGroupInt.and(0x00000fc0).shr(6)
            val index4 = inputGroupInt.and(0x0000003f)
            return ByteGroup(listOf(index1, index2, index3, index4), min(data.size, 3))
        }
    }

    fun toString(dictionary: List<String>, padding: Char? = null): String {
        val output = StringBuilder()
        output.append(dictionary[ir[0]])
        output.append(dictionary[ir[1]])
        output.append(if (bytes > 1) dictionary[ir[2]] else padding ?: "")
        output.append(if (bytes > 2) dictionary[ir[3]] else padding ?: "")
        return output.toString()
    }

    fun toBytes(): ByteArray {
        val outputGroup = ByteArray(bytes)
        val value1 = ir[0]
        val value2 = ir[1]
        val value3 = ir[2]
        val value4 = ir[3]
        // byte shifting madness!
        val byte1 = value1.shl(2) + value2.and(0x30).shr(4)
        outputGroup[0] = byte1.toByte()
        if (bytes > 1) {
            val byte2 = value2.and(0xf).shl(4) + value3.and(0x3c).shr(2)
            outputGroup[1] = byte2.toByte()
        }
        if (bytes > 2) {
            val byte3 = value3.and(0x3).shl(6) + value4.and(0x3f)
            outputGroup[2] = byte3.toByte()
        }
        return outputGroup
    }
}

private fun decode(data: String, dictionary: List<String>, padding: Char?): ByteArray {
    val outputs = mutableListOf<ByteArray>()
    for (index in data.indices step 4) {
        val slice = data.slice(index..min(index + 3, data.length - 1))
        outputs.add(ByteGroup.fromString(slice, dictionary, padding).toBytes())
    }
    val outputSize = max(0, outputs.size - 1) * 3 + (outputs.lastOrNull()?.size ?: 0)
    val output = ByteArray(outputSize)
    outputs.forEachIndexed { index, bytes ->
        bytes.copyInto(output, index * 3)
    }
    return output
}

private fun encode(data: ByteArray, dictionary: List<String>, padding: Char?): String {
    val output = StringBuilder()
    for (index in data.indices step 3) {
        val slice = data.sliceArray(index..min(index + 2, data.size - 1))
        output.append(ByteGroup.fromByteArray(slice).toString(dictionary, padding))
    }
    return output.toString()
}