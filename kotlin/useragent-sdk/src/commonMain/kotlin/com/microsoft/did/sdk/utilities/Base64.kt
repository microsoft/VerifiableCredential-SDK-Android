package com.microsoft.did.sdk.utilities

object Base64 {
    private val dictionary = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
        "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
        "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "/"
    )
    private const val padding = "="
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
        return encode(data, dictionary, "")
    }
    fun decode(base64url: String): ByteArray {
        return decode(base64url, dictionary, "")
    }
}

private fun decode(data: String, dictionary: List<String>, padding: String): ByteArray {
    // 0. Determine the byteArray length required by the modulus or padding
    val numberOfBytes = if (data.length.rem(4) == 0) {
        // no padding required, or padding characters included
        when {
            data.endsWith("$padding$padding") -> { // One byte
                ((data.length / 4) - 1) * 3 + 1
            }
            data.endsWith("$padding") -> { // Two bytes
                ((data.length / 4) - 1) * 3 + 2
            }
            else -> { // Three bytes
                (data.length / 4) * 3
            }
        }
    } else {
        // padding required but no padding character used
        when (data.length.rem(4)) {
            2 -> ((data.length - 2) / 4) * 3 + 1 // One byte in the last input group
            1 -> ((data.length - 1) / 4) * 3 + 2 // Two bytes in the last input group
            0 -> (data.length / 4) * 3 // Three bytes in the last input group
            else -> throw Error("Invalid base64url padding")
        }
    }
    val output = ByteArray(numberOfBytes)

    // 1. iterate through three byte groups
    for (index in 0 until output.size step 3) {
        val indexInString = (index / 3) * 4
        val index1 = data[indexInString]
        val index2 = data[indexInString+1]
        val index3 = data[indexInString+2]
        val index4 = data[indexInString+3]
        // get the index values
        val value1 = dictionary.indexOf(index1.toString())
        val value2 = dictionary.indexOf(index2.toString())
        val value3 = dictionary.indexOf(index3.toString())
        val value4 = dictionary.indexOf(index4.toString())
        // byte shifting madness!
        val byte1 = value1.shl(2) + value2.and(0x30)
        val byte2 = value2.and(0xf).shl(4) + value3.and(0x3c).shr(2)
        val byte3 = value3.and(0x3).shl(6) + value4.and(0x3f)
        output[index] = byte1.toByte()
        output[index+1] = byte2.toByte()
        output[index+2] = byte3.toByte()
    }
    // 2. add the final bytes ignoring padding
    val lastByteGroupInString = (output.size / 3) * 4
    when (output.size.rem(3)) {
        0 -> {
            // No padding, do nothing
        }
        1 -> {
            // get the last two non-padding characters
            val index1 = data[lastByteGroupInString]
            val index2 = data[lastByteGroupInString + 1]
            val value1 = dictionary.indexOf(index1.toString())
            val value2 = dictionary.indexOf(index2.toString())
            val byte1 = value1.shl(2) + value2.shr(4).and(0x3)
            output[output.size - 1] = byte1.toByte()
        }
        2 -> {
            // get the last three non-padding characters
            val index1 = data[lastByteGroupInString]
            val index2 = data[lastByteGroupInString + 1]
            val index3 = data[lastByteGroupInString + 2]
            val value1 = dictionary.indexOf(index1.toString())
            val value2 = dictionary.indexOf(index2.toString())
            val value3 = dictionary.indexOf(index3.toString())
            val byte1 = value1.shl(2) + value2.shr(4).and(0x3)
            val byte2 = value2.and(0xff).shl(4) + value3.and(0x3c).shr(2)
            output[output.size - 2] = byte1.toByte()
            output[output.size - 1] = byte2.toByte()
        }
        else -> throw Error("!!! How did you get 3 from a remainder function on 3")
    }

    return output
}

private fun encode(data: ByteArray, dictionary: List<String>, padding: String): String {
    // 1. Form 24 bit input groups from 3 bytes
    val bytes = data.iterator()
    var inputGrouping = ByteArray(3)
    var inputIndex = 0
    val output =  StringBuilder()

    fun processInputGroup(inputGroup: ByteArray, dictionary: List<String>): String {
//        +--first octet--+-second octet--+--third octet--+
//        |7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|
//        +-----------+---+-------+-------+---+-----------+
//        |5 4 3 2 1 0|5 4 3 2 1 0|5 4 3 2 1 0|5 4 3 2 1 0|
//        +--1.index--+--2.index--+--3.index--+--4.index--+
        // bit shifting is only available in Int and Long. Int is 32, capable of holding all bytes.
        val inputGroupInt = inputGroup[0].toInt().shl(16) + inputGroup[1].toInt().shl(8) + inputGroup[2].toInt()
        val index1 = inputGroupInt.and(0x00fc0000).shr(18)
        val index2 = inputGroupInt.and(0x0003f000).shr(12)
        val index3 = inputGroupInt.and(0x00000fc0).shr(6)
        val index4 = inputGroupInt.and(0x0000003f)
        return "${dictionary[index1]}${dictionary[index2]}${dictionary[index3]}${dictionary[index4]}"
    }
    // process the bytes
    while (bytes.hasNext()) {
        inputGrouping[inputIndex] = bytes.nextByte()
        inputIndex++
        if (inputIndex == 3) {
            output.append(processInputGroup(inputGrouping, dictionary))
            inputIndex = 0
        }
    }
    // add padding if necessary
    if (inputIndex != 0) {
        if (inputIndex == 1) { // we have 1 valid byte
            val inputByte = inputGrouping[0].toInt()
            val index1 = inputByte.and(0xfc).shr(2)
            val index2 = inputByte.and(0x3).shl(4)
            output.append("${dictionary[index1]}${dictionary[index2]}$padding$padding")
        } else if (inputIndex == 2) { // we have 2 valid bytes
            val inputByte = inputGrouping[0].toInt().shl(8) + inputGrouping[1].toInt()
            val index1 = inputByte.and(0xfc00).shr(10)
            val index2 = inputByte.and(0x03f0).shr(4)
            val index3 = inputByte.and(0x000f).shl(2)
            output.append("${dictionary[index1]}${dictionary[index2]}${dictionary[index3]}$padding")
        }
    }

    return output.toString()
}