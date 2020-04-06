package com.microsoft.portableIdentity.sdk.identifier.models.document

import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.LinkedDataKeySpecification
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
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
     * The HEX public key.
     */
    val publicKeyHex: String
) {
    fun toPublicKey(): PublicKey {
        //TODO: Replace dummyKey with public keys in did document once it has been modified from hex to jwk
        val dummyKey = JsonWebKey()
        return when (type) {
            in LinkedDataKeySpecification.RsaSignature2018.values -> {
                return RsaPublicKey(dummyKey)
            }
            in LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values -> {
                return EllipticCurvePublicKey(dummyKey)
            }
            in LinkedDataKeySpecification.EcdsaKoblitzSignature2016.values -> {
                throw SdkLog.error("${LinkedDataKeySpecification.EcdsaKoblitzSignature2016.name} not supported.")
            }
            in LinkedDataKeySpecification.Ed25519Signature2018.values -> {
                throw SdkLog.error("${LinkedDataKeySpecification.Ed25519Signature2018.name} not supported.")
            }
            else -> {
                throw SdkLog.error("Unknown key type: $type")
            }
        }
    }
}
