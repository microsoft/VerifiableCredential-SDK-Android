/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.pairwise

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.util.Base64URL
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PairwiseKeyTest {
    private val suppliedStringForSeedGeneration = "abcdefg"
    private val seed: ByteArray = suppliedStringForSeedGeneration.toByteArray()
    private val seedKey = OctetSequenceKey.Builder(seed).build()

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
        every { keyStore.getKey(Constants.MAIN_IDENTIFIER_REFERENCE) } returns seedKey
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
}