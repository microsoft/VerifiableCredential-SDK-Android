/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.keys.ellipticCurve

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import com.microsoft.did.sdk.util.convertToBigEndian
import com.microsoft.did.sdk.util.generatePublicKeyFromPrivateKey
import com.microsoft.did.sdk.util.publicToXY
import com.microsoft.did.sdk.util.reduceKeySeedSizeAndConvertToUnsigned
import java.nio.ByteBuffer
import java.util.Locale
import javax.inject.Inject

class EllipticCurvePairwiseKey @Inject constructor() {

    fun generate(crypto: CryptoOperations, masterKey: ByteArray, algorithm: Algorithm, peerId: String): PrivateKey {
        val supportedCurves = listOf("P-256K", "SECP256K1")

        if (supportedCurves.indexOf((algorithm as EcKeyGenParams).namedCurve.toUpperCase(Locale.ROOT)) == -1)
            throw PairwiseKeyException("Curve ${algorithm.namedCurve} is not supported")

        val subtleCrypto: SubtleCrypto =
            crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.PRIVATE)

        val pairwiseKeySeedSigned = generatePairwiseSeed(subtleCrypto, masterKey, peerId)
        val pairwiseKeySeedUnsigned = reduceKeySeedSizeAndConvertToUnsigned(pairwiseKeySeedSigned)

        val pubKey = generatePublicKeyFromPrivateKey(pairwiseKeySeedUnsigned)
        val xyData = publicToXY(pubKey)

        val pairwiseKeySeedInBigEndian = convertToBigEndian(pairwiseKeySeedUnsigned)

        return createPairwiseKeyFromPairwiseSeed(algorithm, pairwiseKeySeedInBigEndian, xyData)
    }

    private fun generatePairwiseSeed(subtleCrypto: SubtleCrypto, masterKey: ByteArray, peerId: String): ByteArray {
        // Generate the pairwise seed
        val alg =
            Algorithm(name = W3cCryptoApiConstants.HmacSha512.value)
        val signingKey = JsonWebKey(
            kty = KeyType.Octets.value,
            alg = JoseConstants.Hs512.value,
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
                d = Base64Url.encode(pairwiseKeySeedInBigEndian.array()),
                x = xyData.first.trim(),
                y = xyData.second.trim()
            )
        return EllipticCurvePrivateKey(pairwiseKey)
    }
}