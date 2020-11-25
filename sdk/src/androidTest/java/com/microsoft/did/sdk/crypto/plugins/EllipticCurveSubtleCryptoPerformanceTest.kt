/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.di.defaultTestSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class EllipticCurveSubtleCryptoPerformanceTest {
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val cryptoKeyPair: CryptoKeyPair

    init {
        println("PerfTest->Start init")
        val startTime = getStartTime()
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, defaultTestSerializer)
        val keyReference = "KeyReference1"
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
        println("PerfTest->End init: ${timer(startTime)}")
    }

    fun getStartTime(): Long {
        return System.nanoTime()
    }
    fun timer(start: Long): String {
        val timing = System.nanoTime() - start
        return (timing/1000).toString() + " μs"
    }


    @Test
    fun signAndVerifySignatureTest() {
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )
        println("PerfTest->Start sign")
        val startTime = getStartTime()
        val signedPayload = ellipticCurveSubtleCrypto.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
        println("PerfTest->End sign: ${timer(startTime)}")
        println("PerfTest->Start verify")
        val verified = ellipticCurveSubtleCrypto.verify(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.publicKey, signedPayload, payload)
        println("PerfTest->End verify: ${timer(startTime)}")
        assertThat(verified).isTrue()
    }
/*
    @Test
    fun exportPublicKeyJwk() {
        val actualKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.publicKey)
        val expectedAlgorithm = "ES256K"
        val expectedKeyUsage = KeyUsage.Verify.value
        val expectedKeyType = com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value
        assertThat(actualKey.kty).isEqualTo(expectedKeyType)
        assertThat(actualKey.alg).isEqualTo(expectedAlgorithm)
        assertThat(actualKey.key_ops?.firstOrNull()).isEqualTo(expectedKeyUsage)
    }

    @Test
    fun exportPrivateKeyJwk() {
        val actualKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.privateKey)
        val expectedAlgorithm = "ES256K"
        val expectedKeyUsage = KeyUsage.Sign.value
        val expectedKeyType = com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value
        assertThat(actualKey.kty).isEqualTo(expectedKeyType)
        assertThat(actualKey.alg).isEqualTo(expectedAlgorithm)
        assertThat(actualKey.key_ops?.firstOrNull()).isEqualTo(expectedKeyUsage)
    }

    @Test
    fun importPrivateKey() {
        val jsonWebKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.privateKey)
        val actualCryptoKey =
            ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, jsonWebKey, cryptoKeyPair.privateKey.algorithm, false, listOf(KeyUsage.Sign))
        assertThat(actualCryptoKey.type).isEqualTo(KeyType.Private)
    }

    @Test
    fun importPublicKey() {
        val jsonWebKey = ellipticCurveSubtleCrypto.exportKeyJwk(cryptoKeyPair.publicKey)
        val actualCryptoKey =
            ellipticCurveSubtleCrypto.importKey(
                KeyFormat.Jwk,
                jsonWebKey,
                cryptoKeyPair.publicKey.algorithm,
                false,
                listOf(KeyUsage.Verify)
            )
        assertThat(actualCryptoKey.type).isEqualTo(KeyType.Public)
    }

 */
}