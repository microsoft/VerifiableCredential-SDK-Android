package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.byteArrayToString
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.parseMap

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
    @ImplicitReflectionSerializer
    fun getKid(): String? {
        if (protected.isNotEmpty()) {
            val jsonProtected = Base64Url.decode(protected)
            val mapObject = Json.parseMap<String, String>(byteArrayToString(jsonProtected))
            if (mapObject.containsKey(JoseConstants.Kid.value)) {
                return mapObject[JoseConstants.Kid.value]
            }
        }
        if (header.isNullOrEmpty() && header!!.containsKey(JoseConstants.Kid.value)) {
            return header!![JoseConstants.Kid.value]
        }
        return null
    }
}