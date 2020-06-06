// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import android.util.Base64
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.utilities.controlflow.AlgorithmException
import com.microsoft.did.sdk.utilities.controlflow.KeyException
import com.microsoft.did.sdk.utilities.controlflow.KeyFormatException
import com.microsoft.did.sdk.utilities.controlflow.SignatureException
import com.microsoft.did.sdk.utilities.printBytes
import com.microsoft.did.sdk.utilities.stringToByteArray
import org.bitcoin.NativeSecp256k1
import java.security.SecureRandom
import java.util.*

class Secp256k1Provider(val subtleCryptoSha: SubtleCrypto) : Provider() {

    data class Secp256k1Handle(val alias: String, val data: ByteArray)

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

        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.Sha256,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        val keyPair = CryptoKeyPair(
            privateKey = CryptoKey(
                KeyType.Private,
                extractable,
                signAlgorithm,
                keyUsages.toList(),
                Secp256k1Handle("", secret)
            ),
            publicKey = CryptoKey(
                KeyType.Public,
                true,
                signAlgorithm,
                publicKeyUsage.toList(),
                Secp256k1Handle("", publicKey)
            )
        )

        return return keyPair
    }

    override fun checkGenerateKeyParams(algorithm: Algorithm) {
        val keyGenParams = algorithm as? EcKeyGenParams ?: throw AlgorithmException("EcKeyGenParams expected as algorithm")
        if (keyGenParams.namedCurve.toUpperCase(Locale.ROOT) != W3cCryptoApiConstants.Secp256k1.value.toUpperCase(Locale.ROOT) &&
            keyGenParams.namedCurve.toUpperCase(Locale.ROOT) != W3cCryptoApiConstants.Secp256k1.name.toUpperCase(Locale.ROOT)
        ) {
            throw AlgorithmException("The curve ${keyGenParams.namedCurve} is not supported by Secp256k1Provider")
        }
    }

    override fun onSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }
        return NativeSecp256k1.sign(hashedData, keyData)
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        print("KEY DATA: ")
        printBytes(keyData)

        return NativeSecp256k1.verify(hashedData, signature, keyData)
    }

    override fun onImportKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKey {
        val alias = keyData.kid ?: ""
        return when {
            keyData.d != null -> { // import d as the private key handle
                CryptoKey(
                    type = KeyType.Private,
                    extractable = extractable,
                    algorithm = algorithm,
                    usages = keyUsages.toList(),
                    handle = Secp256k1Handle(
                        alias,
                        Base64.decode(
                            stringToByteArray(keyData.d!!),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        )
                    )
                )
            }
            keyData.k != null -> { // import k as the secret key handle
                CryptoKey(
                    type = KeyType.Secret,
                    extractable = extractable,
                    algorithm = algorithm,
                    usages = keyUsages.toList(),
                    handle = Secp256k1Handle(
                        alias,
                        Base64.decode(
                            stringToByteArray(keyData.k!!),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        )
                    )
                )
            }
            else -> {// public key
                val x =
                    Base64.decode(stringToByteArray(keyData.x!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val y =
                    Base64.decode(stringToByteArray(keyData.y!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val xyData = ByteArray(65)
                xyData[0] = secp256k1Tag.uncompressed.byte
                x.forEachIndexed { index, byte ->
                    xyData[index + 1] = byte
                }
                y.forEachIndexed { index, byte ->
                    xyData[index + 33] = byte
                }
                CryptoKey(
                    type = KeyType.Public,
                    extractable = extractable,
                    algorithm = algorithm,
                    usages = keyUsages.toList(),
                    handle = Secp256k1Handle(alias, xyData)
                )
            }
        }
    }

    override fun onExportKeyJwk(key: CryptoKey): JsonWebKey {
        val keyOps = mutableListOf<String>()
        for (usage in key.usages) {
            keyOps.add(usage.value)
        }
        val publicKey: ByteArray
        val handle = key.handle as Secp256k1Handle
        val d: String? = if (key.type == KeyType.Private) {
            publicKey = NativeSecp256k1.computePubkey(handle.data)
            Base64.encodeToString(handle.data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        } else {
            publicKey = handle.data
            null
        }
        val xyData = publicToXY(publicKey)
        return JsonWebKey(
            kty = com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value,
            kid = handle.alias,
            crv = W3cCryptoApiConstants.Secp256k1.value,
            use = "sig",
            key_ops = keyOps,
            alg = JwaCryptoConverter.webCryptoToJwa(key.algorithm),
            ext = key.extractable,
            d = d?.trim(),
            x = xyData.first.trim(),
            y = xyData.second.trim()
        )
    }

    override fun checkCryptoKey(key: CryptoKey, keyUsage: KeyUsage) {
        super.checkCryptoKey(key, keyUsage)
        if (key.type == KeyType.Private) {
            val keyData = (key.handle as Secp256k1Handle).data
            if (!NativeSecp256k1.secKeyVerify(keyData)) {
                throw KeyException("Private key invalid")
            }
        }
    }

    // mapped from secp256k1_eckey_pubkey_parse
    private fun publicToXY(keyData: ByteArray): Pair<String, String> {
        if (keyData.size == 33 && (
                keyData[0] == secp256k1Tag.even.byte ||
                    keyData[0] == secp256k1Tag.odd.byte)
        ) {
            // compressed form
            throw KeyFormatException("Compressed Hex format is not supported.")
        } else if (keyData.size == 65 && (
                keyData[0] == secp256k1Tag.uncompressed.byte ||
                    keyData[0] == secp256k1Tag.hybridEven.byte ||
                    keyData[0] == secp256k1Tag.hybridOdd.byte
                )
        ) {
            // uncompressed, bytes 1-32, and 33-end are x and y
            val x = keyData.sliceArray(1..32)
            val y = keyData.sliceArray(33..64)
            return Pair(
                Base64.encodeToString(x, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                Base64.encodeToString(y, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            )
        } else {
            throw KeyFormatException("Public key improperly formatted")
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