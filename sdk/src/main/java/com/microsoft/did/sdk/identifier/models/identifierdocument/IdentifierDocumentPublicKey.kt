package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.controlflow.KeyException
import kotlinx.serialization.Serializable

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
    val publicKeyJwk: JsonWebKey
) {
    fun toPublicKey(): PublicKey {
        when (type) {
            in LinkedDataKeySpecification.RsaSignature2018.values -> {
                return RsaPublicKey(this.publicKeyJwk)
            }
            in LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values -> {
                return EllipticCurvePublicKey(this.publicKeyJwk)
            }
            in LinkedDataKeySpecification.EcdsaKoblitzSignature2016.values -> {
                throw KeyException("${LinkedDataKeySpecification.EcdsaKoblitzSignature2016.name} not supported.")
            }
            in LinkedDataKeySpecification.Ed25519Signature2018.values -> {
                throw KeyException("${LinkedDataKeySpecification.Ed25519Signature2018.name} not supported.")
            }
            else -> {
                throw KeyException("Unknown key type: $type")
            }
        }
    }
}
