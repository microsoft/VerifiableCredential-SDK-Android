package com.microsoft.portableIdentity.sdk.crypto.keys.rsa

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithms.RsaHashedKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.utilities.Base64Url

class RsaPairwiseKey {
    companion object {
        val numberOfPrimeTests = 0
        val primeDelegate = ArrayOf()
        fun generate(crypto: CryptoOperations, personaMasterKey: ByteArray, algorithm: RsaHashedKeyGenParams, peerId: String): PrivateKey {
            val keySize = algorithm.modulusLength
            val peerIdByteArray = peerId.toByteArray()
        }

        fun generateDeterministicNumberForPrime(crypto: CryptoOperations, primeSize: Int, personaMasterKey: ByteArray, peerId: String): ByteArray {
            val numberOfRounds = primeSize / (8*64)
            val deterministicKey = "".toByteArray()
            val rounds = []
            for(inx in 0..numberOfRounds) {
                rounds.push() {
                    deterministicKey
                }
            }
        }

        fun generateHashForPrime(crypto: CryptoOperations, inx: Int, key: ByteArray, data: ByteArray, deterministicKey: ByteArray): ByteArray {
            val crypto: SubtleCrypto =
                crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private);
            // Generate the master key
            val alg: Algorithm =
                EcdsaParams(
                    hash = Sha.Sha512
                )
            val signingKey = JsonWebKey(
                kty = KeyType.Octets.value,
                alg = JoseConstants.Hs512.value,
                k = Base64Url.encode(key)
            )
            val importedKey = crypto.importKey(
                KeyFormat.Jwk, signingKey, alg, false, listOf(
                    KeyUsage.Sign
                )
            )
            val signature = crypto.sign(alg, importedKey, data)
            return deterministicKey+signature
        }
    }
}