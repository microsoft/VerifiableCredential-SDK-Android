package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.AesCbcHmacSha2Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.AlgorithmParameters
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Implementation for AES-CBC with HMAC using SHA2
 * @param aesCbcSize One of Aes128CbcHmacSha256, Aes192CbcHmacSha384, Aes256CbcHmacSha512
 */
class AesCbcHmacSha2Provider (private val aesCbcSize: W3cCryptoApiConstants) : Provider() {
    companion object {
        val subtle = Subtle(setOf(
            AesCbcHmacSha2Provider(W3cCryptoApiConstants.Aes128CbcHmacSha256),
            AesCbcHmacSha2Provider(W3cCryptoApiConstants.Aes192CbcHmacSha384),
            AesCbcHmacSha2Provider(W3cCryptoApiConstants.Aes256CbcHmacSha512)
        ), Json.Default)
    }

    override val name: String
        get() = aesCbcSize.value
    override val privateKeyUsage: Set<KeyUsage>?
        get() = emptySet()
    override val publicKeyUsage: Set<KeyUsage>?
        get() = emptySet()
    override val symmetricKeyUsage: Set<KeyUsage>?
        get() = setOf(KeyUsage.Encrypt, KeyUsage.Decrypt)

    private val secondaryKeyLength: Int
        get() = when(aesCbcSize) {
            W3cCryptoApiConstants.Aes128CbcHmacSha256 -> 16
            W3cCryptoApiConstants.Aes192CbcHmacSha384 -> 24
            W3cCryptoApiConstants.Aes256CbcHmacSha512 -> 32
            else -> throw AlgorithmException("AES CBC HMAC initialized with unknown algorithm name ${aesCbcSize.name}")
        }
    private val hmacAlg: String
        get() = when(aesCbcSize) {
        W3cCryptoApiConstants.Aes128CbcHmacSha256 -> W3cCryptoApiConstants.HmacSha256.value
        W3cCryptoApiConstants.Aes192CbcHmacSha384 -> W3cCryptoApiConstants.HmacSha384.value
        W3cCryptoApiConstants.Aes256CbcHmacSha512 -> W3cCryptoApiConstants.HmacSha512.value
        else -> throw AlgorithmException("AES CBC HMAC initialized with unknown algorithm name ${aesCbcSize.name}")
        }

    override fun checkAlgorithmName(algorithm: Algorithm) {
        if (algorithm.name != W3cCryptoApiConstants.AesCbc.value &&
            algorithm.name != name) {
            throw AlgorithmException("Key algorithm to be AES CBC")
        }
    }

    override fun onEncrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val params = algorithm as AesCbcHmacSha2Algorithm;
        return aesCbcHmacEncrypt("key.handle as ByteArray, data, params.aad, params.iv)
    }

    override fun onDecrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val params = algorithm as AesCbcHmacSha2Algorithm;
        if (params.tag == null) {
            throw AlgorithmException("AES-CBC-HMAC-SHA2 requires a tag on decrypt")
        }
        return aesCbcHmacDecrypt(key.handle as ByteArray, params.aad, params.iv, data, params.tag!)
    }

    /**
     * @see https://tools.ietf.org/html/rfc7518#section-5.2.2.2
     */
    private fun aesCbcHmacDecrypt(
        key: ByteArray,
        aad: ByteArray,
        iv: ByteArray,
        ciphertext: ByteArray,
        tag: ByteArray
    ): ByteArray? {
        // 1. Split key into MAC and ENC
        val keys = splitAesKey(key, secondaryKeyLength)
        val macKey = keys.first
        val encKey = keys.second
        // 2. validate integrity
        val al = ByteBuffer.allocate(8)
        al.order(ByteOrder.BIG_ENDIAN)
        al.putLong(8L * aad.size)
        val hmac = Mac.getInstance(hmacAlg)
        hmac.init(macKey)
        hmac.update(aad)
        hmac.update(iv)
        hmac.update(ciphertext)
        val fullTag = hmac.doFinal(al.array())
        for (i in 0 until secondaryKeyLength) {
            if (fullTag[i] != tag[i]) {
                return null
            }
        }
        // 3. decrypt
        val cipherArgs = AlgorithmParameters.getInstance("AES");
        cipherArgs.init(IvParameterSpec(iv))
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, encKey, cipherArgs)
        // 4. plaintext is returned
        return cipher.doFinal(ciphertext)
    }

    /**
     * @see https://tools.ietf.org/html/rfc7518#section-5.2.2.1
     */
    private fun aesCbcHmacEncrypt(
        key: ByteArray,
        plaintext: ByteArray,
        aad: ByteArray,
        iv: ByteArray): ByteArray {
        // 1. Split key into MAC and ENC
        val keys = splitAesKey(key, secondaryKeyLength)
        val macKey = keys.first
        val encKey = keys.second
        // 2. Import IV
        val cipherArgs = AlgorithmParameters.getInstance("AES");
        cipherArgs.init(IvParameterSpec(iv))
        // 3. encrypt plaintext using CBC PKCS #7 padding
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, encKey, cipherArgs)
        val ciphertext = cipher.doFinal(plaintext)
        // 4. AL = #bits in AAD expressed as 64-bit unsigned big-endian integer
        val al = ByteBuffer.allocate(8)
        al.order(ByteOrder.BIG_ENDIAN)
        al.putLong(8L * aad.size)
        // 5. HMAC AAD, IV, ciphertext, AL
        val hmac = Mac.getInstance(hmacAlg)
        hmac.init(macKey)
        hmac.update(aad)
        hmac.update(iv)
        hmac.update(ciphertext)
        val fullTag = hmac.doFinal(al.array())
        // 6. Tag = first TagLength of HMAC
        val tag = fullTag.slice(IntRange(0, secondaryKeyLength))
        // return ciphertext and tag
        return ByteArray(ciphertext.size + tag.size) {
                index ->
            return if (index < ciphertext.size) {
                ciphertext[index]
            } else {
                tag[index - ciphertext.size]
            }
        }
    }

    /**
     * Splits AES key into MAC key and ENC key
     * @returns Pair of keys, first is MAC, second is ENC
     */
    private fun splitAesKey(key: ByteArray, keyLength: Int): Pair<SecretKey, SecretKey> {
        val macKey = SecretKeyFactory.getInstance(hmacAlg).generateSecret(
            SecretKeySpec(
                key.slice(IntRange(0, keyLength)).toByteArray(),
                hmacAlg))
        val encKey = SecretKeyFactory.getInstance("AES").generateSecret(
            SecretKeySpec(
                key.slice(IntRange(key.count() - keyLength, key.count())).toByteArray(),
                "AES"))
        return Pair(macKey, encKey)
    }
}