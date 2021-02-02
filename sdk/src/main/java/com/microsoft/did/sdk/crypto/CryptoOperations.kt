/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.Mac
import javax.crypto.SecretKey

object CryptoOperations {
    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
        Security.insertProviderAt(DidProvider(), Security.getProviders().size + 1)
    }

    fun sign(payload: ByteArray, signingKey: PrivateKey, alg: SigningAlgorithm): ByteArray {
        val signer = if (alg.provider == null) Signature.getInstance(alg.name) else Signature.getInstance(alg.name, alg.provider)
            .apply {
                initSign(signingKey)
                update(payload)
                if (alg.spec != null) setParameter(alg.spec)
            }
        return signer.sign()
    }

    fun verify(payload: ByteArray, publicKey: PublicKey, alg: SigningAlgorithm): Boolean {
        val verifier = if (alg.provider == null) Signature.getInstance(alg.name) else Signature.getInstance(alg.name, alg.provider)
            .apply {
                initVerify(publicKey)
                if (alg.spec != null) setParameter(alg.spec)
            }
        return verifier.verify(payload)
    }

    fun digest(payload: ByteArray, alg: DigestAlgorithm): ByteArray {
        val messageDigest =
            if (alg.provider == null) MessageDigest.getInstance(alg.name) else MessageDigest.getInstance(alg.name, alg.provider)
        return messageDigest.digest(payload)
    }

    fun encrypt(payload: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = if (alg.provider == null) Cipher.getInstance(alg.name) else Cipher.getInstance(alg.name, alg.provider)
        cipher.init(ENCRYPT_MODE, key)
        return cipher.doFinal(payload)
    }

    fun decrypt(payload: ByteArray, key: SecretKey, alg: CipherAlgorithm): ByteArray {
        val cipher = if (alg.provider == null) Cipher.getInstance(alg.name) else Cipher.getInstance(alg.name, alg.provider)
        cipher.init(DECRYPT_MODE, key)
        return cipher.doFinal(payload)
    }

    fun computeMac(payload: ByteArray, key: SecretKey, alg: MacAlgorithm): ByteArray {
        val mac = if (alg.provider == null) Mac.getInstance(alg.name) else Mac.getInstance(alg.name, alg.provider)
        mac.init(key)
        return mac.doFinal(payload)
    }

    fun generateKeyPair(alg: KeyGenAlgorithm): KeyPair {
        val keyGen = if (alg.provider == null) KeyPairGenerator.getInstance(alg.name) else KeyPairGenerator.getInstance(alg.name, alg.provider)
        keyGen.initialize(alg.spec)
        return keyGen.genKeyPair()
    }

    inline fun <reified T : Key> generateKey(alg: PrivateKeyFactoryAlgorithm): T {
        val factory = if (alg.provider == null) KeyFactory.getInstance(alg.name) else KeyFactory.getInstance(alg.name, alg.provider)
        return factory.generatePrivate(alg.keySpec) as T
    }

    inline fun <reified T : Key> generateKey(alg: PublicKeyFactoryAlgorithm): T {
        val factory = if (alg.provider == null) KeyFactory.getInstance(alg.name) else KeyFactory.getInstance(alg.name, alg.provider)
        return factory.generatePublic(alg.keySpec) as T
    }

    /**
     * Generates a 256 bit seed.
     */
    fun generateSeed(): ByteArray {
        val randomNumberGenerator = SecureRandom()
        return randomNumberGenerator.generateSeed(16)
    }
}