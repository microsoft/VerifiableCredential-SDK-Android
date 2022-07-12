package com.microsoft.did.sdk.crypto.protocols.jose

import com.microsoft.did.sdk.util.controlflow.ValidatorException

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
            throw ValidatorException("JWS contains no key id")
        }
    }
}