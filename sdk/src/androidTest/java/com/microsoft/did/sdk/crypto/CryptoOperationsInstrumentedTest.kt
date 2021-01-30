/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.di.defaultTestSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class CryptoOperationsInstrumentedTest {
    private val androidSubtle: SubtleCrypto
    private val keyStore: EncryptedKeyStore
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
    private val keyRef: String = "TestKeysCryptoOperations"
    private val crypto: CryptoOperations

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        keyStore = EncryptedKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()
        crypto = CryptoOperations(androidSubtle, keyStore, ellipticCurvePairwiseKey)
    }

    @Test
    fun generateEllipticCurveKeyPairTest() {
        val publicKey = crypto.generateKeyPair(keyRef, KeyType.EllipticCurve)
        val publicKeyJWK = publicKey.toJWK()
        assertThat(publicKeyJWK.kid).isNotNull
        val expectedKeyType = "EC"
        val actualKeyType = publicKeyJWK.kty
        assertThat(actualKeyType).isEqualTo(expectedKeyType)
    }

    @Test
    fun generateRSAKeyPairTest() {
        val publicKey = crypto.generateKeyPair(keyRef, KeyType.RSA)
        val publicKeyJWK = publicKey.toJWK()
        assertThat(publicKeyJWK.kid).isNotNull
        val expectedKeyType = "RSA"
        val actualKeyType = publicKeyJWK.kty
        assertThat(actualKeyType).isEqualTo(expectedKeyType)
    }
}