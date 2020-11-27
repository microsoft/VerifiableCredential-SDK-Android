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
    private val testName = "EllipticCurveSubtleCryptoPerformanceTest"
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val cryptoKeyPair: CryptoKeyPair
    private val payload = ByteArray(2048) {it.toByte()}

    init {
        println("PerfTest->(${getTestName()}) - Start init")
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
        println("PerfTest->(${getTestName()}) - End init: ${timer(startTime)}")
    }

    fun getTestName(): String {
        return this.testName
    }

    fun getStartTime(): Long {
        return System.nanoTime()
    }

    fun timer(start: Long): String {
        val timing = System.nanoTime() - start
        return (timing/1000).toString()
    }


    @Test
    fun signAndVerifySignatureTest() {
        for (loop in 0..9) {
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): Start sign: 0")
            val startTime = getStartTime()
            val signedPayload = ellipticCurveSubtleCrypto.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): End sign: ${timer(startTime)}")
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): Start verify: 0")
            val verified = ellipticCurveSubtleCrypto.verify(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.publicKey, signedPayload, payload)
            println("PerfTest->(${getTestName()}) in  μs - (${loop}): End verify: ${timer(startTime)}")
            assertThat(verified).isTrue()
        }
    }
}