package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.crypto.protocols.jose.jws.serialization.JwkSerializer
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyConverter
import kotlinx.serialization.Serializable
import java.security.PublicKey

/**
 * Data Class for defining a Public Key in Identifier Document in Jwk format which can be used for signing/encryption
 */
@Serializable
data class IdentifierDocumentPublicKey(
    /**
     * The id of the public key in the format
     * {keyIdentifier}
     */
    val id: String,

    /**
     * The type of the public key.
     */
    val type: String,

    /**
     * The owner of the key.
     */
    val controller: String? = null,

    /**
     * The JWK public key.
     */
    @Serializable(with = JwkSerializer::class)
    val publicKeyJwk: JWK
) {
    fun toPublicKey(): PublicKey {
        return KeyConverter.toJavaKeys(listOf(publicKeyJwk)).first() as PublicKey
    }
}
