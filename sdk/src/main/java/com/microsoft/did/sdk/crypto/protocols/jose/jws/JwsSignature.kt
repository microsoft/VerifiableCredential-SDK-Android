package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.byteArrayToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * JWS signature used by the general JSON
 */
@Serializable
data class JwsSignature(
    /**
     * The protected (signed) header.
     */
    val protected: String,

    /**
     * The unprotected (unverified) header.
     */
    val header: Map<String, String>?,

    /**
     * The JWS Signature
     */
    val signature: String
) {
    fun getKid(serializer: Json): String? {
        return getMember(JoseConstants.Kid.value, serializer)
    }

    fun getAlg(serializer: Json): String? {
        return getMember(JoseConstants.Alg.value, serializer)
    }

    private fun getMember(member: String, serializer: Json): String? {
        if (protected.isNotEmpty()) {
            val jsonProtected = Base64Url.decode(protected)
            val mapObject =
                serializer.decodeFromString(MapSerializer(String.serializer(), String.serializer()), byteArrayToString(jsonProtected))
            if (mapObject.containsKey(member)) {
                return mapObject[member]
            }
        }
        if (!header.isNullOrEmpty() && header.containsKey(member)) {
            return header[member]
        }
        return null
    }
}