package com.microsoft.portableIdentity.sdk.identifier.deprecated.document

import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import kotlinx.serialization.Serializable

/**
 * Data Class for defining an Identifier Document
 * Public Key.
 */
@Serializable
data class IdentifierDocumentPublicKey (
    /**
     * The id of the public key in the format
     * #{keyIdentifier}.
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

    @Deprecated("against spec", ReplaceWith("this.controller"))
    val owner: String? = null,

    /**
     * The JWK public key.
     */
    val publicKeyJwk: JsonWebKey
    ) {

    fun toPublicKey(logger: ILogger): PublicKey {
        return when (type) {
            in LinkedDataKeySpecification.RsaSignature2018.values -> {
                return RsaPublicKey(this.publicKeyJwk, logger = logger)
            }
            in LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values -> {
                return EllipticCurvePublicKey(this.publicKeyJwk, logger = logger)
            }
            in LinkedDataKeySpecification.EcdsaKoblitzSignature2016.values -> {
                throw logger.error("${LinkedDataKeySpecification.EcdsaKoblitzSignature2016.name} not supported.")
            }
            in LinkedDataKeySpecification.Ed25519Signature2018.values -> {
                throw logger.error("${LinkedDataKeySpecification.Ed25519Signature2018.name} not supported.")
            }
            else -> {
                throw logger.error("Unknown key type: $type")
            }
        }
    }
}