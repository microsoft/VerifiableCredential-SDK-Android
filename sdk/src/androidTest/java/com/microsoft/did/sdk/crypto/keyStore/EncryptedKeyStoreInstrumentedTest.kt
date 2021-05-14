/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.keyStore

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import com.nimbusds.jose.jwk.JWK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class EncryptedKeyStoreInstrumentedTest {

    private val keyStore = EncryptedKeyStore(InstrumentationRegistry.getInstrumentation().targetContext)

    private val signingJwk =
        JWK.parse("{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"kid\":\"signKey\",\"x\":\"dqEeSHC5KhsMSW_Zh8kBzQXB9HLgZqzBtmkAh-tAw4U\",\"y\":\"Yo_a4_sB2METsA9YRD6II_PjbHiWg4gwqQJiOxx4Suk\"}")
    private val updateJwk =
        JWK.parse("{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"kid\":\"updateKey\",\"x\":\"iK9EveBFpO_KltBpKJfvq2KA2da-_VmYOwhJCAk6pRM\",\"y\":\"45wg6vkIT4JXcwzEJBjEWsJWxWv9cLXhdOAA4CACb8k\"}")

    @Test
    fun saveAndGet() {
        keyStore.storeKey("key1", signingJwk)
        val actualKey = keyStore.getKey("key1")
        assertThat(signingJwk).isEqualToComparingFieldByFieldRecursively(actualKey)
    }

    @Test
    fun overwriteKey() {
        keyStore.storeKey("key2", signingJwk)
        keyStore.storeKey("key2", updateJwk)
        val actualKey = keyStore.getKey("key2")
        assertThat(actualKey).isEqualToComparingFieldByFieldRecursively(updateJwk)
    }

    @Test(expected = KeyStoreException::class)
    fun unknownKey() {
        keyStore.getKey("key55")
    }

}