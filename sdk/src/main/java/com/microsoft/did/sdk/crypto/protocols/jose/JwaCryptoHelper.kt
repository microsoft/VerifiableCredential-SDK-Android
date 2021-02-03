package com.microsoft.did.sdk.crypto.protocols.jose

object JwaCryptoHelper {
    fun extractDidAndKeyId(keyId: String): Pair<String?, String> {
        val matches = Regex("^([^#]*)#(.+)$").matchEntire(keyId)
        return if (matches != null) {
            Pair(
                if (matches.groupValues[1].isNotBlank()) {
                    matches.groupValues[1]
                } else {
                    null
                }, matches.groupValues[2]
            )
        } else {
            Pair(null, keyId)
        }
    }
}