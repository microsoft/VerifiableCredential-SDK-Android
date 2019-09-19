package com.microsoft.did.sdk.crypto.plugins

import android.util.Base64
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import org.bitcoin.NativeSecp256k1
import java.security.SecureRandom
import java.util.*

class Secp256k1Provider(): Provider() {
    companion object {
        init {
            System.loadLibrary("secp256k1")
        }
    }

    override val name: String = "ECDSA"
    override val privateKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Sign)
    override val publicKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Verify)
    override val symmetricKeyUsage: Set<KeyUsage>? = null

    override fun onGenerateKeyPair(
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKeyPair {
        val seed = ByteArray(32)
        val random = SecureRandom()
        random.nextBytes(seed)
        NativeSecp256k1.randomize(seed)

        val secret = ByteArray(32)
        random.nextBytes(secret)

        val publicKey = NativeSecp256k1.computePubkey(secret)

        val keyPair = CryptoKeyPair(CryptoKey(
            KeyType.Private,
            extractable,
            algorithm,
            keyUsages.toList(),
            secret
        ), CryptoKey(
            KeyType.Public,
            true,
            algorithm,
            publicKeyUsage.toList(),
            publicKey
        ))

        return return keyPair
    }

    override fun checkGenerateKeyParams(algorithm: Algorithm) {
        val keyGenParams = algorithm as? EcKeyGenParams ?: throw Error("EcKeyGenParams expected as algorithm")
        if (keyGenParams.namedCurve.toUpperCase(Locale.ROOT) != W3cCryptoApiConstants.Secp256k1.value.toUpperCase(Locale.ROOT) &&
            keyGenParams.namedCurve.toUpperCase(Locale.ROOT) != W3cCryptoApiConstants.Secp256k1.name.toUpperCase(Locale.ROOT)) {
            throw Error("The curve ${keyGenParams.namedCurve} is not supported by Secp256k1Provider")
        }
    }

    override fun onSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val keyData = getKeyData(key)
        if (data.size !== 32) {
            throw Error("Data must be 32 bytes")
        }

        return NativeSecp256k1.sign(data, keyData)
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val keyData = getKeyData(key)
        if (data.size !== 32) {
            throw Error("Data must be 32 bytes")
        }

        return NativeSecp256k1.verify(data, signature, keyData)
    }

    override fun onExportKeyJwk(key: CryptoKey): JsonWebKey {
        val keyOps = mutableListOf<String>()
        for (usage in key.usages) {
            keyOps.add(usage.value)
        }
        var publicKey = key.handle as? ByteArray
        val d: String? = if (key.type == KeyType.Private) {
            publicKey = NativeSecp256k1.computePubkey(key.handle as? ByteArray)
            Base64.encodeToString(key.handle as? ByteArray, Base64.URL_SAFE)
        } else {
            null
        }
        if (publicKey == null) {
            throw Error("No public key components could be found")
        }
//        val xyData = publicToXY(publicKey)
        return JsonWebKey(
            kty = com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value,
            crv = W3cCryptoApiConstants.Secp256k1.value,
            use = "sig",
            key_ops = keyOps,
            alg = this.name,
            ext = key.extractable,
            d = d,
            x = "todo",
            y = "todo"
        )
    }

    override fun checkCryptoKey(key: CryptoKey, keyUsage: KeyUsage) {
        super.checkCryptoKey(key, keyUsage)
        if (key.type == KeyType.Private) {
            val keyData = getKeyData(key)
            if (!NativeSecp256k1.secKeyVerify(keyData)) {
                throw Error("Private key invalid")
            }
        }
    }

    private fun getKeyData(key: CryptoKey): ByteArray {
        checkAlgorithmName(key.algorithm)
        return key.handle as? ByteArray ?: throw Error("Invalid key")
    }

    // mapped from secp256k1_eckey_pubkey_parse
    private fun publicToXY(keyData: ByteArray): Pair<ULongArray, ULongArray> {
        if (keyData.size == 33 && (
                    keyData[0] == secp256k1Tag.even.byte ||
                    keyData[0] == secp256k1Tag.odd.byte)) {
            // compressed form
            return Pair(
                ulongArrayOf(0UL),
                ulongArrayOf(0UL)
            )

        } else if (keyData.size == 65 && (
                    keyData[0] == secp256k1Tag.uncompressed.byte ||
                    keyData[0] == secp256k1Tag.hybridEven.byte ||
                    keyData[0] == secp256k1Tag.hybridOdd.byte
                    )) {
            // uncompressed
            return Pair(
                ulongArrayOf(0UL),
                ulongArrayOf(0UL)
            )
        } else {
            throw Error("Public key improperly formatted")
        }
    }

    enum class secp256k1Tag(val byte: Byte) {
        even(0x02),
        odd(0x03),
        uncompressed(0x04),
        hybridEven(0x06),
        hybridOdd(0x07)
    }
}