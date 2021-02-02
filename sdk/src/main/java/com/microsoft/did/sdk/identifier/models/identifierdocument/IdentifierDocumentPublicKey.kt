package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.KeyAlgorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.nimbusds.jose.util.Base64URL
import kotlinx.serialization.Serializable
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec

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
                val n = Base64URL.from(publicKeyJwk.n).decodeToBigInteger()
                val e = Base64URL.from(publicKeyJwk.e).decodeToBigInteger()
                return CryptoOperations.generatePublicKey(KeyAlgorithm.RSAPublic(RSAPublicKeySpec(n, e)))
            }
            in LinkedDataKeySpecification.EcdsaSecp256k1Signature2019.values -> {
                val x = Base64URL.from(publicKeyJwk.x).decodeToBigInteger()
                val y = Base64URL.from(publicKeyJwk.y).decodeToBigInteger()
                return CryptoOperations.generatePublicKey(KeyAlgorithm.Secp256k1Public(x, y))
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
