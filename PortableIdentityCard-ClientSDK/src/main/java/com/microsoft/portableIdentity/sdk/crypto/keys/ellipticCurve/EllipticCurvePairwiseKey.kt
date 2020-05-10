/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve

import android.util.Base64
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.Secp256k1Provider
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import org.bitcoin.NativeSecp256k1
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EllipticCurvePairwiseKey {

    companion object {
        //TODO: Verify the list of supported curves. Is K-256 supported?
        private val supportedCurves = listOf("K-256", "P-256K")

        fun generate(crypto: CryptoOperations, personaMasterKey: ByteArray, algorithm: Algorithm, peerId: String): PrivateKey {
            val crypto: SubtleCrypto =
                crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private);
            // Generate the master key
            val alg =
                Algorithm(name = W3cCryptoApiConstants.HmacSha256.value)
            val signingKey = JsonWebKey(
                kty = KeyType.Octets.value,
                alg = JoseConstants.Hs256.value,
                k = Base64Url.encode(personaMasterKey)
            )
            val key = crypto.importKey(
                KeyFormat.Jwk, signingKey, alg, false, listOf(
                    KeyUsage.Sign
                )
            )
            val pairwiseKeySeed = crypto.sign(alg, key, peerId.map { it.toByte() }.toByteArray())

            if (supportedCurves.indexOf((algorithm as EcKeyGenParams).namedCurve) == -1)
                throw SdkLog.error("Curve ${algorithm.namedCurve} is not supported")

            val pubKey = NativeSecp256k1.computePubkey(pairwiseKeySeed)
            val xyData = publicToXY(pubKey)

            val byteBuffer = ByteBuffer.allocate(32)
            byteBuffer.put(pairwiseKeySeed)
            byteBuffer.order(ByteOrder.BIG_ENDIAN)
            byteBuffer.array()

            val pairwiseKey =
                JsonWebKey(
                    //Generate a kid
                    kty = KeyType.EllipticCurve.value,
                    crv = algorithm.namedCurve,
                    alg = EcdsaParams(
                        hash = Algorithm(
                            name = W3cCryptoApiConstants.Sha256.value
                        ),
                        additionalParams = mapOf(
                            "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
                        )
                    ).name,
                    d = Base64Url.encode(byteBuffer.array()),
                    x = xyData.first.trim(),
                    y = xyData.second.trim()
                )
            return EllipticCurvePrivateKey(pairwiseKey)
        }

        private fun publicToXY(keyData: ByteArray): Pair<String, String> {
            if (keyData.size == 33 && (
                    keyData[0] == Secp256k1Provider.secp256k1Tag.even.byte ||
                        keyData[0] == Secp256k1Provider.secp256k1Tag.odd.byte)
            ) {
                // compressed form
                return Pair(
                    "",
                    ""
                )
            } else if (keyData.size == 65 && (
                    keyData[0] == Secp256k1Provider.secp256k1Tag.uncompressed.byte ||
                        keyData[0] == Secp256k1Provider.secp256k1Tag.hybridEven.byte ||
                        keyData[0] == Secp256k1Provider.secp256k1Tag.hybridOdd.byte
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
                throw SdkLog.error("Public key improperly formatted")
            }
        }
    }
}