package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.utilities.*
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
    fun getKid(logger: ILogger): String? {
        return getMember(JoseConstants.Kid.value, logger = logger)
    }
    
    fun getAlg(logger: ILogger): String? {
        return getMember(JoseConstants.Alg.value, logger = logger)
    }
    
    private fun getMember(member: String, logger: ILogger): String? {
        if (protected.isNotEmpty()) {
            val jsonProtected = Base64Url.decode(protected, logger = logger)
            val polymorphicSerialization: IPolymorphicSerialization = PolymorphicSerialization
            val mapObject = polymorphicSerialization.parseMap<String, String>(byteArrayToString(jsonProtected), String::class, String::class)
//            val mapObject = MinimalJson.serializer.parseMap<String, String>(byteArrayToString(jsonProtected))
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