/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.crypto.keyStore

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.SecretKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.RsaHashedKeyAlgorithm
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class AndroidKeyStoreInstrumentedTest {

    private val keyStore: AndroidKeyStore
    private val keyRef: String = "TestKeys"
    private var actualPublicKey: RsaPublicKey
    private var actualPrivateKey: RsaPrivateKey

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = Serializer()
        keyStore = AndroidKeyStore(context, serializer)
        val androidSubtle = AndroidSubtle(keyStore)
        val keyPair = androidSubtle.generateKeyPair(
            RsaHashedKeyAlgorithm(
                modulusLength = 4096UL,
                publicExponent = 65537UL,
                hash = Sha.Sha256,
                additionalParams = mapOf("KeyReference" to keyRef)
            ), false, listOf(KeyUsage.Encrypt, KeyUsage.Decrypt)
        )
        actualPublicKey = RsaPublicKey(androidSubtle.exportKeyJwk(keyPair.publicKey))
        actualPrivateKey = RsaPrivateKey(androidSubtle.exportKeyJwk(keyPair.privateKey))
    }

    @Test
    fun savePublicAndPrivateKeyPairTest() {
        keyStore.save(keyRef, actualPublicKey)
        keyStore.save(keyRef, actualPrivateKey)

        val expectedPublicKeyById = keyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualToComparingFieldByFieldRecursively(expectedPublicKeyById)

        val expectedPrivateKeyById = keyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualToComparingFieldByFieldRecursively(expectedPrivateKeyById)
    }

    @Test
    fun listKeysTest() {
        keyStore.save(keyRef, actualPrivateKey)
        keyStore.save(keyRef, actualPublicKey)
        val keysInKeyStore = keyStore.list()
        val expectedPrivateKey = keysInKeyStore[keyRef]
        assertThat(expectedPrivateKey!!.getLatestKeyId()).isNotNull()
        assertThat(actualPrivateKey.kid).isEqualTo(expectedPrivateKey.kids.firstOrNull())
        val expectedPublicKey = keysInKeyStore[keyRef]
        assertThat(actualPublicKey.kid).isEqualTo(expectedPublicKey!!.kids.firstOrNull())
    }

    @Test
    fun invalidKeyReferenceTest() {
        val nonExistingPublicKeyRef = "kid1"
        assertThatThrownBy { keyStore.getPublicKey(nonExistingPublicKeyRef) }.isInstanceOf(RuntimeException::class.java)
    }

    @Test
    fun getPublicKeyByReferenceTest() {
        keyStore.save(keyRef, actualPublicKey)
        val expectedPublicKey = keyStore.getPublicKey(keyRef)
        assertThat(actualPublicKey.key).isEqualTo(expectedPublicKey.keys[0].key)
    }

    @Test
    fun getPublicKeyByIdTest() {
        keyStore.save(keyRef, actualPublicKey)
        val expectedPublicKey = keyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualToComparingFieldByFieldRecursively(expectedPublicKey)
    }

    @Test
    fun getPrivateKeyByReferenceTest() {
        keyStore.save(keyRef, actualPrivateKey)
        val expectedPrivateKey = keyStore.getPrivateKey(keyRef)
        assertThat(actualPrivateKey.key).isEqualTo(expectedPrivateKey.keys[0].key)
    }

    @Test
    fun getPrivateKeyByIdTest() {
        keyStore.save(keyRef, actualPrivateKey)
        val expectedPrivateKey = keyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualToComparingFieldByFieldRecursively(expectedPrivateKey)
    }

    @Test
    fun checkOrCreateKidTest() {
        keyStore.save(keyRef, actualPrivateKey)
        val kid = keyStore.checkOrCreateKeyId(keyRef, null)
        assertThat(kid).startsWith("#$keyRef.")
    }

    @Test
    fun checkOrCreateKidFirstKeyTest() {
        val kid = keyStore.checkOrCreateKeyId(keyRef, null)
        assertThat(kid).startsWith("#$keyRef.1")
    }

    @Test
    fun saveAndGetSecretKeyTest() {
        val secretKey = SecretKey(
            JsonWebKey(
                kty = KeyType.Octets.value,
                kid = "#$keyRef.1"
            )
        )
        keyStore.save(keyRef, secretKey)
        val actualSecretKey = keyStore.getSecretKey(keyRef)
        assertThat(actualSecretKey.keys.firstOrNull()).isEqualToComparingFieldByFieldRecursively(secretKey)
    }

    @Test
    fun getSecretKeyByIdTest() {
        val secretKey = SecretKey(
            JsonWebKey(
                kty = KeyType.Octets.value,
                kid = "#secret.2"
            )
        )
        keyStore.save("secret", secretKey)
        val actualSecretKey = keyStore.getSecretKeyById(secretKey.kid)
        assertThat(actualSecretKey).isEqualToComparingFieldByFieldRecursively(secretKey)
    }

}