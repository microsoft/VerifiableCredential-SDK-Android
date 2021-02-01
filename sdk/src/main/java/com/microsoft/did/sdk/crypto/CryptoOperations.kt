/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.*
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

object CryptoOperations {
    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
        Security.insertProviderAt(DidProvider(), Security.getProviders().size + 1)
    }

    fun sign(payload: ByteArray, keyId: String, alg: SigningAlgorithm): ByteArray {
        val signingKey = EncryptedKeyStore.getKey<PrivateKey>(keyId)
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
        val publicKey = EncryptedKeyStore.getKey<PublicKey>(keyId)
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
        val secretKey = EncryptedKeyStore.getKey<SecretKey>(keyId)
        return encrypt(payload, secretKey, alg)
    }

    fun encrypt(payload: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = Cipher.getInstance(alg.name, alg.provider)
        cipher.init(ENCRYPT_MODE, key)
        return cipher.doFinal(payload)
    }

    fun decrypt(payload: ByteArray, keyId: String, alg: CipherAlgorithm): ByteArray {
        val secretKey = EncryptedKeyStore.getKey<SecretKey>(keyId)
        return encrypt(payload, secretKey, alg)
    }

    fun decrypt(payload: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = Cipher.getInstance(alg.name, alg.provider)
        cipher.init(DECRYPT_MODE, key)
        return cipher.doFinal(payload)
    }

    fun computeMac(payload: ByteArray, key: SecretKey, alg: MacAlgorithm): ByteArray {
        val mac = Mac.getInstance(alg.name, alg.provider)
        mac.init(key)
        return mac.doFinal(payload)
    }

    fun generateKeyPair(keyId: String, alg: KeyGenAlgorithm): PublicKey {
        val keyGen = KeyPairGenerator.getInstance(alg.name, alg.provider)
        keyGen.initialize(alg.spec)
        val keyPair = keyGen.genKeyPair()
        EncryptedKeyStore.storeKeyPair(keyPair, keyId)
        return keyPair.public
    }

    fun generatePrivateKey(alg: KeyAlgorithm): PrivateKey {
        val factory = KeyFactory.getInstance(alg.name, alg.provider)
        return factory.generatePrivate(alg.keySpec)
    }

    fun generatePublicKey(alg: KeyAlgorithm): PublicKey {
        val factory = KeyFactory.getInstance(alg.name, alg.provider)
        return factory.generatePublic(alg.keySpec)
    }

    /**
     * Generates and stores a 256 bit seed.
     */
    fun generateSeed(persona: String) {
        val randomNumberGenerator = SecureRandom()
        val seed = randomNumberGenerator.generateSeed(16)
        val secretKey = SecretKeySpec(seed, "RAW")
        EncryptedKeyStore.storeSecretKey(secretKey, persona)
    }

    fun getSeed(persona: String): ByteArray {
        return EncryptedKeyStore.getKey<SecretKeySpec>(persona).encoded
    }
}