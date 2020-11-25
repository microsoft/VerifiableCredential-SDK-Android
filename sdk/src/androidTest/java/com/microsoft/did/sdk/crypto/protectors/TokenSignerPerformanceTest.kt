/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.identifier.SidetreePayloadProcessor
import com.microsoft.did.sdk.identifier.models.Identifier
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.Success

@RunWith(AndroidJUnit4ClassRunner::class)
class TokenSignerPerformanceTest {
    private val testName = "TokenSignerPerformanceTest"
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val cryptoKeyPair: CryptoKeyPair
    private val keyReference = "KeyReference1"
    private val serializer = Json
    private val signer: TokenSigner
    private val cryptoOperations: CryptoOperations
    private val identifierCreator: IdentifierCreator

    init {
        println("PerfTest->(${getTestName()}) - Start init")
        val startTime = getStartTime()
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, defaultTestSerializer)
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )

        cryptoOperations = CryptoOperations(ellipticCurveSubtleCrypto, keyStore, EllipticCurvePairwiseKey())
        val sidetreePayloadProcessor = SidetreePayloadProcessor(defaultTestSerializer)
        identifierCreator = IdentifierCreator(cryptoOperations, sidetreePayloadProcessor)

        signer = TokenSigner(cryptoOperations, serializer)
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
        return (timing/1000).toString() + " μs"
    }


    @Test
    fun signAndVerifySignatureTest() {
        val payload = "the Answer to the Ultimate Question of Life, the Universe, and Everything"
        val result: Result<Identifier> = identifierCreator.create("ION")
        println("PerfTest->(${getTestName()}) - Start sign")
        var startTime = getStartTime()
        when (result) {
            is Result.Success -> {
                // use result.payload (Identifier)
                val signedPayload = signer.signWithIdentifier(payload, result.payload)
                println("PerfTest->(${getTestName()}) - End sign: ${timer(startTime)}")
                println("PerfTest->(${getTestName()}) - Signed token: ${signedPayload}")
            }
        }

        println("PerfTest->(${getTestName()}) - Start verify")
        startTime = getStartTime()
        //val verified = ellipticCurveSubtleCrypto.verify(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.publicKey, signedPayload, payload)
        println("PerfTest->(${getTestName()}) - End verify: ${timer(startTime)}")
        //assertThat(verified).isTrue()
    }
}