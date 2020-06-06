/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.keys.ellipticCurve

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.Secp256k1Provider
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.controlflow.KeyFormatException
import com.microsoft.did.sdk.utilities.controlflow.PairwiseKeyException
import org.bitcoin.NativeSecp256k1
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class EllipticCurvePairwiseKey @Inject constructor() {

    fun generate(crypto: CryptoOperations, masterKey: ByteArray, algorithm: Algorithm, peerId: String): PrivateKey {
        //TODO: Verify the list of supported curves. Is K-256 supported?
        val supportedCurves = listOf("K-256", "P-256K")

        val subtleCrypto: SubtleCrypto =
            crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private);

        val pairwiseKeySeed = generatePairwiseSeed(subtleCrypto, masterKey, peerId)

        if (supportedCurves.indexOf((algorithm as EcKeyGenParams).namedCurve) == -1)
            throw PairwiseKeyException("Curve ${algorithm.namedCurve} is not supported")

        val pubKey = NativeSecp256k1.computePubkey(pairwiseKeySeed)
        val xyData = publicToXY(pubKey)

        val pairwiseKeySeedInBigEndian = convertPairwiseSeedToBigEndian(pairwiseKeySeed)

        return createPairwiseKeyFromPairwiseSeed(algorithm, pairwiseKeySeedInBigEndian, xyData)
    }

    private fun generatePairwiseSeed(subtleCrypto: SubtleCrypto, masterKey: ByteArray, peerId: String): ByteArray {
        // Generate the pairwise seed
        val alg =
            Algorithm(name = W3cCryptoApiConstants.HmacSha256.value)
        val signingKey = JsonWebKey(
            kty = KeyType.Octets.value,
            alg = JoseConstants.Hs256.value,
            k = Base64Url.encode(masterKey)
        )
        val key = subtleCrypto.importKey(
            KeyFormat.Jwk, signingKey, alg, false, listOf(
                KeyUsage.Sign
            )
        )
        return subtleCrypto.sign(alg, key, peerId.map { it.toByte() }.toByteArray())
    }

    private fun createPairwiseKeyFromPairwiseSeed(
        algorithm: Algorithm,
        pairwiseKeySeedInBigEndian: ByteBuffer,
        xyData: Pair<String, String>
    ): PrivateKey {
        val pairwiseKey =
            JsonWebKey(
                kty = KeyType.EllipticCurve.value,
                crv = (algorithm as EcKeyGenParams).namedCurve,
                alg = EcdsaParams(
                    hash = Algorithm(
                        name = W3cCryptoApiConstants.Sha256.value
                    ),
                    additionalParams = mapOf(
                        "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
                    )
                ).name,
                d = Base64Url.encode(pairwiseKeySeedInBigEndian.array()),
                x = xyData.first.trim(),
                y = xyData.second.trim()
            )
        return EllipticCurvePrivateKey(pairwiseKey)
    }

    // Converts the public key returned by NativeSecp256K1 library from byte array to x and y co-ordinates to be used in JWK
    private fun publicToXY(keyData: ByteArray): Pair<String, String> {
        //TODO: Confirm if we get back public key in compressed format from NativeSecp256k1
        return when {
            // compressed hex format of Elliptic Curve public key
            isPublicKeyCompressedHex(keyData) -> throw KeyFormatException("Compressed Hex format is not supported.")
            // Convert uncompressed hex and hybrid hex formats of public key to x and y co-ordinates to be used in JWK format
            isPublicKeyUncompressedOrHybridHex(keyData) -> publicKeyToXYForUncompressedOrHybridHex(keyData)
            else -> throw PairwiseKeyException("Public key improperly formatted")
        }
    }

    private fun publicKeyToXYForUncompressedOrHybridHex(keyData: ByteArray): Pair<String, String> {
        // uncompressed, bytes 1-32, and 33-end are x and y
        val x = keyData.sliceArray(1..32)
        val y = keyData.sliceArray(33..64)
        return Pair(
            Base64.encodeToString(x, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
            Base64.encodeToString(y, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        )
    }

    private fun isPublicKeyUncompressedOrHybridHex(keyData: ByteArray): Boolean {
        return keyData.size == 65 && (keyData[0] == Secp256k1Provider.secp256k1Tag.uncompressed.byte ||
            keyData[0] == Secp256k1Provider.secp256k1Tag.hybridEven.byte ||
            keyData[0] == Secp256k1Provider.secp256k1Tag.hybridOdd.byte
            )
    }

    private fun isPublicKeyCompressedHex(keyData: ByteArray): Boolean {
        return (keyData.size == 33 && (
            keyData[0] == Secp256k1Provider.secp256k1Tag.even.byte ||
                keyData[0] == Secp256k1Provider.secp256k1Tag.odd.byte)
            )
    }

    private fun convertPairwiseSeedToBigEndian(pairwiseKeySeed: ByteArray): ByteBuffer {
        val pairwiseKeySeedInBigEndian = ByteBuffer.allocate(32)
        pairwiseKeySeedInBigEndian.put(pairwiseKeySeed)
        pairwiseKeySeedInBigEndian.order(ByteOrder.BIG_ENDIAN)
        pairwiseKeySeedInBigEndian.array()
        return pairwiseKeySeedInBigEndian
    }
}