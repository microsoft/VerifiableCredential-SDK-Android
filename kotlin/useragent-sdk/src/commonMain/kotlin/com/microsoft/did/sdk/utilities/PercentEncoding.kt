package com.microsoft.did.sdk.utilities

import kotlin.collections.Map

object PercentEncoding {
    private val restrictedCharacters = mapOf(
        ":" to "%3A",
        "/" to "%2F",
        "?" to "%3F",
        "#" to "$23",
        "[" to "%5B",
        "]" to "%5D",
        "@" to "%40",
        "!" to "%21",
        "$" to "%24",
        "&" to "%26",
        "'" to "%27",
        "(" to "%28",
        ")" to "%29",
        "*" to "%2A",
        "+" to "%2B",
        "," to "%2C",
        ";" to "%3B",
        "=" to "%3D",
        "%" to "%25",
        "\\n" to "%0A",
        "\\r" to "%0D",
        " " to "%20"
    )
    private val additionalEncodings = mapOf(
        "-" to "%2D",
        "." to "%2E",
        "_" to "%5F",
        "~" to "%7E"
    )
    private val numerics = generatePercentMap(48, 57)
    private val uppercaseAlpha = generatePercentMap(65, 90)
    private val lowercaseAlpha = generatePercentMap(97, 122)
    private val decodeCharacters = listOf(restrictedCharacters, additionalEncodings, numerics, uppercaseAlpha, lowercaseAlpha)

    private val findPercentEncode = Regex("\\%([0-9A-Fa-f]{2})")

    fun encode(data: String, logger: ILogger): String {
        return data.map {
            if (restrictedCharacters.containsKey("$it")) {
                restrictedCharacters["$it"] ?: logger.error("Map contains $it but does not contain $it ¯\\_(ツ)_/¯")
            } else {
                "$it"
            }
        }.joinToString(separator = "")
    }

    fun decode(data: String, logger: ILogger): String {
        val finds = findPercentEncode.findAll(data)
        val iterator = finds.iterator()
        var previousIndex = 0
        val builder = StringBuilder()
        while (iterator.hasNext()) {
            val find = iterator.next()
            val index = find.range.first
            builder.append(data.slice((previousIndex) until index))
            builder.append(decodePercent(find.groupValues[1], logger))
            previousIndex = find.range.last + 1
        }
        if (previousIndex != data.length) {
            builder.append(data.slice((previousIndex) until data.length))
        }
        return builder.toString()
    }

    private fun decodePercent(hex: String, logger: ILogger): Char {
        val mapping = decodeCharacters.map {
            it.entries.firstOrNull { mapping ->
                mapping.value.toUpperCase() == hex.toUpperCase()
            }
        }.reduce {
                acc, mapping ->
            if (mapping != null && acc == null) {
                mapping
            } else {
                acc
            }
        } ?: throw logger.error("Illegal Percent encoding $hex")
        return mapping.key[0]
    }

    /**
     * from (inclusive)
     * to (inclusive)
     */
    private fun generatePercentMap(from: Int, to: Int): Map<String, String> {
        if (to < from) {
            throw Error("To must be less than from")
        }
        val map = mutableMapOf<String, String>()
        for (i in from..to) {
            val char = i.toChar()
            val hex = i.toString(16)
            val percent = "%$hex"
            map["$char"] =percent
        }
        return map
    }
}