/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.KeyTypeFactory
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.spi.EcPairwiseKeySpec
import com.microsoft.did.sdk.crypto.spi.SignatureSpi
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.SecretKey

open class SigningAlgorithm(val name: String, val provider: String, val spec: AlgorithmParameterSpec? = null) {
    class Secp256k1 : SigningAlgorithm("SHA256WITHPLAIN-ECDSA", "SC")
}

open class DigestAlgorithm(val name: String, val provider: String) {
    class Sha256 : DigestAlgorithm("SHA-256", "SC")
    class Rsa : DigestAlgorithm("SHA-512", "SC") // EXAMPLE
}

open class CipherAlgorithm(val name: String, val provider: String) {
    class DesCbcPkcs5Padding : DigestAlgorithm("DES/CBC/PKCS5Padding", "SC") // EXAMPLE
}

open class KeyAlgorithm(val name: String, val provider: String, val keySpec: KeySpec) {
    class Secp256(ecPrivateKeySpec: ECPrivateKeySpec) : KeyAlgorithm("EC", "SC", ecPrivateKeySpec) // EXAMPLE
    class EcPairwise(ecPairwiseKeySpec: EcPairwiseKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwiseKeySpec) // EXAMPLE
}

class CryptoOperations(
    private val keyStore: KeyStore,
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
) {
    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
        Security.insertProviderAt(DidProvider(), Security.getProviders().size + 1)
    }

    fun sign(payload: ByteArray, keyId: String, alg: SigningAlgorithm): ByteArray {
        val signingKey = keyStore.getKey<PrivateKey>(keyId)
        return sign(payload, signingKey, alg)
    }

    fun sign(payload: ByteArray, signingKey: PrivateKey, alg: SigningAlgorithm): ByteArray {
        val signer = Signature.getInstance(alg.name, alg.provider)
            .apply {
                initSign(signingKey)
                update(payload)
                if (alg.spec != null) setParameter(alg.spec)
            }
        return signer.sign()
    }

    fun verify(payload: ByteArray, keyId: String, alg: SigningAlgorithm): Boolean {
        val publicKey = keyStore.getKey<PublicKey>(keyId)
        return verify(payload, publicKey, alg)
    }

    fun verify(payload: ByteArray, publicKey: PublicKey, alg: SigningAlgorithm): Boolean {
        val verifier = Signature.getInstance(alg.name, alg.provider)
            .apply {
                initVerify(publicKey)
                if (alg.spec != null) setParameter(alg.spec)
            }
        return verifier.verify(payload)
    }

    fun digest(payload: ByteArray, alg: DigestAlgorithm): ByteArray {
        val messageDigest = MessageDigest.getInstance(alg.name, alg.provider)
        return messageDigest.digest(payload)
    }

    fun encrypt(payload: ByteArray, keyId: String, alg: CipherAlgorithm): ByteArray {
        val secretKey = keyStore.getKey<SecretKey>(keyId)
        return encrypt(payload, secretKey, alg)
    }

    fun encrypt(payload: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = Cipher.getInstance(alg.name, alg.provider)
        cipher.init(ENCRYPT_MODE, key)
        return cipher.doFinal(payload)
    }

    fun decrypt(payload: ByteArray, keyId: String, alg: CipherAlgorithm): ByteArray {
        val secretKey = keyStore.getKey<SecretKey>(keyId)
        return encrypt(payload, secretKey, alg)
    }

    fun decrypt(payload: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = Cipher.getInstance(alg.name, alg.provider)
        cipher.init(DECRYPT_MODE, key)
        return cipher.doFinal(payload)
    }

    fun generatePrivateKey(alg: KeyAlgorithm): PrivateKey {
        val keyFactory = KeyFactory.getInstance(alg.name, alg.provider)
        return keyFactory.generatePrivate(alg.keySpec)
    }

//    /**
//     * Generates a KeyPair with the given provider.
//     *
//     * Stores the private key in the KeyStore with given keyId and returns the corresponding public key.
//     */
//    fun generateKeyPair(keyId: String, signatureSpi: SignatureSpi): PublicKey {
//        val keyPair = signatureSpi.generateKeyPair()
//        keyStore.saveKey(keyPair.private, keyId)
//        return keyPair.public
//    }
//
//    /**
//     * Generate a pairwise key for the specified algorithms
//     * @param algorithm for the key
//     * @param seedReference Reference to the seed
//     * @param userDid Id for the user
//     * @param peerId Id for the peer
//     * @returns the pairwise private key
//     */
//    fun generatePairwise(algorithm: Algorithm, seedReference: String, userDid: String, peerId: String): PrivateKey {
//        val masterKey: ByteArray = this.generatePersonaMasterKey(seedReference, userDid)
//
//        return when (val keyType = KeyTypeFactory.createViaWebCrypto(algorithm)) {
//            KeyType.EllipticCurve -> ellipticCurvePairwiseKey.generate(this, masterKey, algorithm, peerId)
//            else -> throw PairwiseKeyException("Pairwise key for type '${keyType.value}' is not supported.")
//        }
//    }
//
//    /**
//     * Generates a 256 bit seed.
//     */
//    fun generateAndStoreSeed() {
//        val randomNumberGenerator = SecureRandom()
//        val seed = randomNumberGenerator.generateSeed(16)
//        val secretKey = SecretKey(JsonWebKey(k = Base64Url.encode(seed)))
//        keyStore.saveKey(AndroidConstants.masterSeed.value, secretKey)
//    }
//
//    /**
//     * Generate a pairwise master key.
//     * @param seedReference  The master seed for generating pairwise keys
//     * @param userDid  The owner DID
//     * @returns the master key for the user
//     */
//    fun generatePersonaMasterKey(seedReference: String, userDid: String): ByteArray {
//        // Set of master keys for the different persona's
//        val masterKeys: MutableMap<String, ByteArray> = mutableMapOf()
//
//        var masterKey: ByteArray? = masterKeys[userDid]
//        if (masterKey != null)
//            return masterKey
//
//        // Get the seed
//        val jwk = keyStore.getSecretKey(seedReference)
//
//        keyStore.getKey<SecretKey>(seedReference)
//        masterKey = generateMasterKeyFromSeed(jwk, userDid)
//        masterKeys[userDid] = masterKey
//        return masterKey
//    }
//
//    private fun generateMasterKeyFromSeed(jwk: KeyContainer<SecretKey>, userDid: String): ByteArray {
//        // Get the subtle crypto
//        val crypto: SubtleCrypto =
//            subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.PRIVATE)
//
//        // Generate the master key
//        val alg = Algorithm(name = W3cCryptoApiConstants.HmacSha512.value)
//        val masterJwk = JsonWebKey(
//            kty = KeyType.Octets.value,
//            alg = JoseConstants.Hs512.value,
//            k = jwk.getKey().k
//        )
//        val key = crypto.importKey(
//            KeyFormat.Jwk, masterJwk, alg, false, listOf(
//                KeyUsage.Sign
//            )
//        )
//        return crypto.sign(alg, key, userDid.map { it.toByte() }.toByteArray())
//    }
}