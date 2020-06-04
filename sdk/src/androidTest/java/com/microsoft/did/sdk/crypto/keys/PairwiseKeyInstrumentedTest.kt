/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.keys

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.Serializer
import com.microsoft.did.sdk.utilities.controlflow.PairwiseKeyException
import com.microsoft.did.sdk.utilities.stringToByteArray
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class PairwiseKeyInstrumentedTest {
    private val androidSubtle: SubtleCrypto
    private val ellipticCurveSubtleCrypto: SubtleCrypto
    private val keyStore: AndroidKeyStore
    private var crypto: CryptoOperations
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
    private val seedReference = "masterSeed"

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = Serializer()
        keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, serializer)
        crypto = CryptoOperations(androidSubtle, keyStore, ellipticCurvePairwiseKey)
        val suppliedStringForSeedGeneration = "abcdefg"
        val seed = SecretKey(
            JsonWebKey(
                kty = KeyType.Octets.value,
                k = Base64Url.encode(stringToByteArray(suppliedStringForSeedGeneration))
            )
        )
        keyStore.save(seedReference, seed)
    }

    @Test
    fun generatePersonaMasterKeyTest() {
        val expectedEncodedMasterKey = "h-Z5gO1eBjY1EYXh64-f8qQF5ojeh1KVMKxmd0JI3YKScTOYjVm-h1j2pUNV8q6s8yphAR4lk5yXYiQhAOVlUw"
        var persona = "persona"
        var masterKey = crypto.generatePersonaMasterKey(seedReference, persona)
        var actualEncodedMasterKey = Base64Url.encode(masterKey)
        assertThat(actualEncodedMasterKey).isEqualTo(expectedEncodedMasterKey)

        masterKey = crypto.generatePersonaMasterKey(seedReference, persona)
        actualEncodedMasterKey = Base64Url.encode(masterKey)
        assertThat(actualEncodedMasterKey).isEqualTo(expectedEncodedMasterKey)

        persona = "persona1"
        masterKey = crypto.generatePersonaMasterKey(seedReference, persona)
        actualEncodedMasterKey = Base64Url.encode(masterKey)
        assertThat(actualEncodedMasterKey).isNotEqualTo(expectedEncodedMasterKey)
    }

    @Test
    fun generateDeterministicECPairwiseKey() {
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.Sha256
            )
        )
        val persona = "did:persona"
        val peer = "did:peer"

        // Generate pairwise key
        val ecPairwiseKey = crypto.generatePairwise(alg, seedReference, persona, peer)
        keyStore.save("key", ecPairwiseKey)

        //Use the pairwise key generated to sign and verify just to make sure it is successful. Verify doesn't return anything to make assertions on that. If verification fails, test would fail automatically.
        val ecAlgorithm = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val data = stringToByteArray("1234567890")
        crypto = CryptoOperations(ellipticCurveSubtleCrypto, keyStore, ellipticCurvePairwiseKey)

        val signature = crypto.sign(data, "key", ecAlgorithm)
        val verify = crypto.verify(data, signature, "key", ecAlgorithm);
    }

    @Test
    fun generateUniquePairwiseKeyUsingDifferentSeed() {
        val results = Array<String?>(50){""}
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.Sha256
            )
        )
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        for(i in 0 .. 49) {
            val keyReference = "key-$i"
            val keyValue = SecretKey(
                JsonWebKey(
                    kty = KeyType.Octets.value,
                    k = Base64Url.encode(stringToByteArray("1234567890-$i"))
                )
            )
            keyStore.save(seedReference, keyValue)
            keyStore.save(keyReference, keyValue)

            val actualPairwiseKey = crypto.generatePairwise(alg, keyReference, persona, peer)
            results[i] = (actualPairwiseKey as EllipticCurvePrivateKey).d
            assertThat(results.filter { value -> value == actualPairwiseKey.d }.size).isEqualTo(1)
        }
    }

    @Test
    fun generateUniquePairwiseKeyUsingDifferentPeer() {
        val results = Array<String?>(50){""}
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.Sha256
            )
        )
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        for(i in 0 .. 49) {
            val suppliedPeer = "$peer-$i"
            val actualPairwiseKey = crypto.generatePairwise(alg, seedReference, persona, suppliedPeer)
            results[i] = (actualPairwiseKey as EllipticCurvePrivateKey).d
            assertThat(results.filter { value -> value == actualPairwiseKey.d }.size).isEqualTo(1)
        }
    }

    @Test
    fun invalidCurveSuppliedForECPairwiseKeyGeneration() {
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256r1.value,
            additionalParams = mapOf(
                "hash" to Sha.Sha256
            )
        )
        val persona = "did:persona"
        val peer = "did:peer"
        Assertions.assertThatThrownBy { crypto.generatePairwise(alg, seedReference, persona, peer) }.isInstanceOf(PairwiseKeyException::class.java)
    }

    @Test
    fun invalidAlgorithmSuppliedForECPairwiseKeyGeneration() {
        val invalidAlgorithmName = "Hmac"
        val alg = Algorithm(invalidAlgorithmName)
        val persona = "did:persona"
        val peer = "did:peer"
        Assertions.assertThatThrownBy { crypto.generatePairwise(alg, seedReference, persona, peer) }.isInstanceOf(PairwiseKeyException::class.java)
    }
}