/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.keys

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.PrivateKeyFactoryAlgorithm
import com.microsoft.did.sdk.crypto.PublicKeyFactoryAlgorithm
import com.microsoft.did.sdk.crypto.SigningAlgorithm
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.spi.EcPairwisePrivateKeySpec
import com.microsoft.did.sdk.crypto.spi.EcPairwisePublicKeySpec
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.util.Base64URL
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

@RunWith(AndroidJUnit4ClassRunner::class)
class PairwiseKeyInstrumentedTest {
    private var crypto: CryptoOperations
    private val suppliedStringForSeedGeneration = "abcdefg"
    private val seed: ByteArray = suppliedStringForSeedGeneration.toByteArray()
    private val seedKey = OctetSequenceKey.Builder(seed).build()

    private val inputStream: InputStream

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        inputStream = context.assets.open("Pairwise.EC.json")
        crypto = CryptoOperations
    }

    /**
     * Tests if pairwise key generated with the same master seed, persona id and peer id is same every time
     */
    @Test
    fun generateSamePairwiseKeyTest() {
//        val persona = "did:persona:1"
        val peer = "did:peer:1"
        val privateParams = PrivateKeyFactoryAlgorithm.EcPairwise(EcPairwisePrivateKeySpec(seed, peer))
        val pairwiseKey1 = crypto.generateKey<ECPrivateKey>(privateParams)
        val pairwiseKey2 = crypto.generateKey<ECPrivateKey>(privateParams)
        assertThat(pairwiseKey1).isEqualTo(pairwiseKey2)
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
        val keyStore: EncryptedKeyStore = mockk()
        every { keyStore.getKey(Constants.MASTER_IDENTIFIER_NAME) } returns seedKey
        val creator = IdentifierCreator(mockk(), mockk(), mockk(), keyStore)
        var masterKey = creator.generatePersonaSeed(persona)
        var actualEncodedMasterKey = Base64URL.encode(masterKey)
        assertThat(actualEncodedMasterKey.toString()).isEqualTo(expectedEncodedMasterKey)

        masterKey = creator.generatePersonaSeed(persona)
        actualEncodedMasterKey = Base64URL.encode(masterKey)
        assertThat(actualEncodedMasterKey.toString()).isEqualTo(expectedEncodedMasterKey)

        persona = "persona1"
        masterKey = creator.generatePersonaSeed(persona)
        actualEncodedMasterKey = Base64URL.encode(masterKey)
        assertThat(actualEncodedMasterKey.toString()).isNotEqualTo(expectedEncodedMasterKey)
    }

    /**
     * Generate deterministic pairwise key and test if it is capable of signing
     */
    @Test
    fun generateDeterministicECPairwiseKey() {
        val peer = "did:peer"

        // Generate pairwise key
        val ecPairwiseKey = crypto.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.EcPairwise(
            EcPairwisePrivateKeySpec(seed, peer)
        ))
        val ecPairwisePublic = crypto.generateKey<ECPublicKey>(PublicKeyFactoryAlgorithm.EcPairwise(
            EcPairwisePublicKeySpec(ecPairwiseKey)
        ))

        //Use the pairwise key generated to sign and verify just to make sure it is successful. Verify doesn't return anything to make assertions on that. If verification fails, test would fail automatically.

        val data = "1234567890".toByteArray()

        val signature = crypto.sign(data, ecPairwiseKey, SigningAlgorithm.Secp256k1())
        assertThat(crypto.verify(data, signature, ecPairwisePublic,  SigningAlgorithm.Secp256k1())).isTrue;
    }

    /**
     * Generate pairwise keys with different master seed but same persona id and peer id as input and verifies if pairwise keys generated are unique
     */
    @Test
    fun generateUniquePairwiseKeyUsingDifferentSeed() {
        val results = Array<String?>(50) { "" }
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        val keyStore: EncryptedKeyStore = mockk()
        val creator = IdentifierCreator(mockk(), mockk(), mockk(), keyStore)
        for (i in 0..49) {
            val seed ="1234567890-$i".toByteArray()
            every { keyStore.getKey(Constants.MASTER_IDENTIFIER_NAME) } returns OctetSequenceKey.Builder(seed).build()
            val generatedSeed = creator.generatePersonaSeed(persona)
            val pairwiseKey = crypto.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.EcPairwise(
                EcPairwisePrivateKeySpec(generatedSeed, peer)
            ))
            val privateKeySecret = pairwiseKey.s.toString(16)
            results[i] = privateKeySecret
            assertThat(results.filter { value -> value == privateKeySecret }.size).isEqualTo(1)
        }
    }

    /**
     * Generate pairwise keys with different peer id but same persona id and master seed as input and verifies if pairwise keys generated are unique
     */
    @Test
    fun generateUniquePairwiseKeyUsingDifferentPeer() {
        val results = Array<String?>(50) { "" }
        val persona = "did:persona:1"
        val peer = "did:peer:1"
        val keyStore: EncryptedKeyStore = mockk()
        val creator = IdentifierCreator(mockk(), mockk(), mockk(), keyStore)
        every { keyStore.getKey(Constants.MASTER_IDENTIFIER_NAME) } returns seedKey
        val personaSeed = creator.generatePersonaSeed(persona)
        for (i in 0..49) {
            val suppliedPeer = "$peer-$i"
            val actualPrivate = crypto.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.EcPairwise(
                EcPairwisePrivateKeySpec(personaSeed, suppliedPeer)
            ))
            val privateKeySecret = actualPrivate.s.toString(16)
            results[i] = privateKeySecret
            assertThat(results.filter { value -> value == privateKeySecret }.size).isEqualTo(1)
        }
    }

    /**
     * Using test vectors for master seed, peer id and persona id,
     * generates pairwise keys and verifies if pairwise keys generated match the keys in test vector file
     */
    @Test
    fun generateSameKeysInFile() {
        val countOfIds = 10
        val testKeysJsonString = inputStream.bufferedReader().readText()
        val testPairwiseKeys = defaultTestSerializer.decodeFromString(TestKeys.serializer(), testKeysJsonString)
        val seed = "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi".toByteArray()
        val keyStore: EncryptedKeyStore = mockk()
        val creator = IdentifierCreator(mockk(), mockk(), mockk(), keyStore)
        every { keyStore.getKey(Constants.MASTER_IDENTIFIER_NAME) } returns OctetSequenceKey.Builder(seed).build()
        val persona = "abcdef"
        val personaSeed = creator.generatePersonaSeed(persona)
        for (index in 0 until countOfIds) {
            val pairwiseKey = crypto.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.EcPairwise(
                EcPairwisePrivateKeySpec(personaSeed, testPairwiseKeys.keys[index].pwid)
            ))
            val privateKeySecret = Base64URL.encode(pairwiseKey.s).toString()
            assertThat(testPairwiseKeys.keys[index].key).isEqualTo(privateKeySecret)
            assertThat(1).isEqualTo(testPairwiseKeys.keys.filter { it.key == privateKeySecret }.size)
        }
    }
}