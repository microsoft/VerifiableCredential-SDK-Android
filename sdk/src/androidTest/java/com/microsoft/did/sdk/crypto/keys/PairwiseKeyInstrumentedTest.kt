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
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import com.microsoft.did.sdk.util.stringToByteArray
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4ClassRunner::class)
class PairwiseKeyInstrumentedTest {
    private val androidSubtle: SubtleCrypto
    private val ellipticCurveSubtleCrypto: SubtleCrypto
    private val keyStore: AndroidKeyStore
    private var crypto: CryptoOperations
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
    private val seedReference = "masterSeed"

    private val inputStream: InputStream

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        inputStream = context.assets.open("Pairwise.EC.json")
        keyStore = AndroidKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, defaultTestSerializer)
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

    /**
     * Tests if pairwise key generated with the same master seed, persona id and peer id is same every time
     */
    @Test
    fun generateSamePairwiseKeyTest() {
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm
            )
        )
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        val pairwiseKey1 = crypto.generatePairwise(alg, seedReference, persona, peer)
        val pairwiseKey2 = crypto.generatePairwise(alg, seedReference, persona, peer)
        assertThat(pairwiseKey1.getPublicKey()).isEqualToComparingFieldByFieldRecursively(pairwiseKey2.getPublicKey())
    }

    /**
     * Using a test vector for master seed and master key,
     * a) verifies if master key generated from same master seed and persona id is same every time
     * b) verifies if master key generated from same master seed but different persona id is different from test vector
     */

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

    /**
     * Generate deterministic pairwise key and test if it is capable of signing
     */
    @Test
    fun generateDeterministicECPairwiseKey() {
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm
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
        crypto.verify(data, signature, "key", ecAlgorithm);
    }

    /**
     * Generate pairwise keys with different master seed but same persona id and peer id as input and verifies if pairwise keys generated are unique
     */
    @Test
    fun generateUniquePairwiseKeyUsingDifferentSeed() {
        val results = Array<String?>(50) { "" }
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm
            )
        )
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        for (i in 0..49) {
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

    /**
     * Generate pairwise keys with different peer id but same persona id and master seed as input and verifies if pairwise keys generated are unique
     */
    @Test
    fun generateUniquePairwiseKeyUsingDifferentPeer() {
        val results = Array<String?>(50) { "" }
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm
            )
        )
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        for (i in 0..49) {
            val suppliedPeer = "$peer-$i"
            val actualPairwiseKey = crypto.generatePairwise(alg, seedReference, persona, suppliedPeer)
            results[i] = (actualPairwiseKey as EllipticCurvePrivateKey).d
            assertThat(results.filter { value -> value == actualPairwiseKey.d }.size).isEqualTo(1)
        }
    }

    /**
     * Using test vectors for master seed, peer id and persona id,
     * generates pairwise keys and verifies if pairwise keys generated match the keys in test vector file
     */
    @Test
    fun generateSameKeysInFile() {
        val countOfIds = 10
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm
            )
        )
        val testKeysJsonString = inputStream.bufferedReader().readText()
        val testPairwiseKeys = defaultTestSerializer.decodeFromString(TestKeys.serializer(), testKeysJsonString)
        val seed =
            SecretKey(JsonWebKey(k = Base64Url.encode(stringToByteArray("xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi"))))
        val seedReference = "masterkey"
        keyStore.save(seedReference, seed)
        for (index in 0 until countOfIds) {
            val persona = "abcdef"
            val pairwiseKey = crypto.generatePairwise(alg, seedReference, persona, index.toString())
            assertThat(testPairwiseKeys.keys[index].key).isEqualTo((pairwiseKey as EllipticCurvePrivateKey).d)
            assertThat(1).isEqualTo(testPairwiseKeys.keys.filter { it.key == (pairwiseKey).d }.size)
        }
    }

    /**
     * Verifies if test fails when in invalid curve is used for pairwise key generation
     */
    @Test
    fun invalidCurveSuppliedForECPairwiseKeyGeneration() {
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256r1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm
            )
        )
        val persona = "did:persona"
        val peer = "did:peer"
        Assertions.assertThatThrownBy { crypto.generatePairwise(alg, seedReference, persona, peer) }
            .isInstanceOf(PairwiseKeyException::class.java)
    }

    /**
     * Verifies if test fails when in invalid algorithm is used for pairwise key generation
     */
    @Test
    fun invalidAlgorithmSuppliedForECPairwiseKeyGeneration() {
        val invalidAlgorithmName = "Hmac"
        val alg = Algorithm(invalidAlgorithmName)
        val persona = "did:persona"
        val peer = "did:peer"
        Assertions.assertThatThrownBy { crypto.generatePairwise(alg, seedReference, persona, peer) }
            .isInstanceOf(PairwiseKeyException::class.java)
    }
}