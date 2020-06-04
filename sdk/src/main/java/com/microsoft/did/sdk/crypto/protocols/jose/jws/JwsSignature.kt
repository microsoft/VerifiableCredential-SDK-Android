package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.utilities.*
import kotlinx.serialization.Serializable

/**
 * JWS signature used by the general JSON
 */
@Serializable
data class JwsSignature (
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
    fun getKid(serializer: Serializer): String? {
        return getMember(JoseConstants.Kid.value, serializer)
    }
    
    fun getAlg(serializer: Serializer): String? {
        return getMember(JoseConstants.Alg.value, serializer)
    }
    
    private fun getMember(member: String, serializer: Serializer): String? {
        if (protected.isNotEmpty()) {
            val jsonProtected = Base64Url.decode(protected)
            val mapObject = serializer.parseMap(byteArrayToString(jsonProtected), String::class, String::class)
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