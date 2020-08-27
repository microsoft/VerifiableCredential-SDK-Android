package com.microsoft.did.sdk.crypto.protocols.jose

import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaHashedKeyAlgorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaOaepParams
import com.microsoft.did.sdk.util.controlflow.CryptoException
import java.util.Locale

object JwaCryptoConverter {
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
            Pair(null, keyId)
        }
    }

    fun jwaAlgToWebCrypto(algorithm: String): Algorithm {
        return when (algorithm.toUpperCase(Locale.ENGLISH)) {
            JoseConstants.Rs256.value, JoseConstants.Rs384.value, JoseConstants.Rs512.value -> {
                // get hash size
                //TODO: Throws NumberFormatException rarely. Try to reproduce the scenario it happens and fix it.
                val hashSize = Regex("[Rr][Ss](\\d+)").matchEntire(algorithm)!!.groupValues[0]
                Algorithm(
                    name = W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
                    additionalParams = mapOf("hash" to Sha.get(hashSize.toInt()))
                )
            }
            JoseConstants.RsaOaep.value, JoseConstants.RsaOaep256.value -> {
                RsaOaepParams(
                    additionalParams = mapOf("hash" to Algorithm(name = W3cCryptoApiConstants.Sha256.value))
                )
            }
            JoseConstants.EcDsa.value, JoseConstants.Es256K.value -> {
                EcdsaParams(
                    hash = Algorithm(name = W3cCryptoApiConstants.Sha256.value),
                    additionalParams = mapOf(
                        "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
                    )
                )
            }
            JoseConstants.EdDsa.value -> {
                EcdsaParams(
                    hash = Algorithm(name = W3cCryptoApiConstants.Sha256.value),
                    additionalParams = mapOf("namedCurve" to W3cCryptoApiConstants.Ed25519.value)
                )
            }
            else -> Algorithm(name = algorithm)
        }
    }

    fun webCryptoToJwa(algorithm: Algorithm): String {
        return when (algorithm) {
            is EcdsaParams -> {
                when (algorithm.additionalParams["namedCurve"]) {
                    W3cCryptoApiConstants.Secp256k1.value -> {
                        when (algorithm.hash.name) {
                            Sha.SHA256.algorithm.name -> "ES256K"
                            Sha.SHA384.algorithm.name -> "ES384K"
                            Sha.SHA512.algorithm.name -> "ES512K"
                            else -> "ES256K"
                        }
                    }
                    else -> {
                        when (algorithm.hash.name) {
                            Sha.SHA256.algorithm.name -> "ES256"
                            Sha.SHA384.algorithm.name -> "ES384"
                            Sha.SHA512.algorithm.name -> "ES512"
                            else -> "ES256"
                        }
                    }
                }
            }
            else -> throw CryptoException("Unknown algorithm: ${algorithm.name}")
        }
    }

    fun jwkAlgToKeyGenWebCrypto(algorithm: String): Algorithm {
        return when (algorithm.toUpperCase(Locale.ENGLISH)) {
            JoseConstants.Rs256.value, JoseConstants.Rs384.value, JoseConstants.Rs512.value -> {
                // get hash size
                val hashSize = Regex("[Rr][Ss](\\d+)").matchEntire(algorithm)!!.groupValues[0]
                RsaHashedKeyAlgorithm(
                    hash = Sha.get(hashSize.toInt()),
                    publicExponent = 65537UL,
                    modulusLength = 4096UL // KEY SIZE
                )
            }
            JoseConstants.RsaOaep.value, JoseConstants.RsaOaep256.value -> {
                RsaOaepParams(
                    additionalParams = mapOf(
                        "hash" to Algorithm(
                            name = W3cCryptoApiConstants.Sha256.value
                        )
                    )
                )
            }
            JoseConstants.Es256K.value -> {
                EcdsaParams(
                    hash = Algorithm(
                        name = W3cCryptoApiConstants.Sha256.value
                    ),
                    additionalParams = mapOf(
                        "namedCurve" to W3cCryptoApiConstants.Secp256k1.value,
                        "format" to "DER"
                    )
                )
            }
            JoseConstants.EdDsa.value -> {
                EcdsaParams(
                    hash = Algorithm(
                        name = W3cCryptoApiConstants.Sha256.value
                    ),
                    additionalParams = mapOf(
                        "namedCurve" to W3cCryptoApiConstants.Ed25519.value
                    )
                )
            }
            else -> {
                throw CryptoException("Unknown JOSE algorithm: $algorithm")
            }
        }
    }
}