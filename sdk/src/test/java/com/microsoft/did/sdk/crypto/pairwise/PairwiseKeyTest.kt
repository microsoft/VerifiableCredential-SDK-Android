/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.pairwise

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
import java.io.File
import java.io.InputStream
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey

class PairwiseKeyTest {
    private var crypto: CryptoOperations = CryptoOperations
    private val suppliedStringForSeedGeneration = "abcdefg"
    private val seed: ByteArray = suppliedStringForSeedGeneration.toByteArray()
    private val seedKey = OctetSequenceKey.Builder(seed).build()
    private val inputStream: InputStream = File(".\\src\\test\\assets\\Pairwise.EC.json").inputStream()

    /**
     * Tests if pairwise key generated with the same master seed, persona id and peer id is same every time
     */
    @Test
    fun generateSamePairwiseKeyTest() {
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
    fun `verify pairwise persona seed generation`() {
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

    @Test
    fun `generate pairwise KeyPair, sign and verify`() {
        val peer = "did:peer"

        val ecPairwiseKey = crypto.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.EcPairwise(
            EcPairwisePrivateKeySpec(seed, peer)
        ))
        val ecPairwisePublic = crypto.generateKey<ECPublicKey>(PublicKeyFactoryAlgorithm.EcPairwise(
            EcPairwisePublicKeySpec(ecPairwiseKey)
        ))

        val data = "1234567890".toByteArray()
        val signature = crypto.sign(data, ecPairwiseKey, SigningAlgorithm.ES256K)
        assertThat(crypto.verify(data, signature, ecPairwisePublic,  SigningAlgorithm.ES256K)).isTrue;
    }

    /**
     * Generate pairwise keys with different master seed but same persona id and peer id as input and verifies if pairwise keys generated are unique
     */
    @Test
    fun `verify unique pairwise generation for unique master seed`() {
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
    fun `verify unique pairwise generation for unique peer`() {
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
    fun `verify pairwise key generation using test vectors`() {
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