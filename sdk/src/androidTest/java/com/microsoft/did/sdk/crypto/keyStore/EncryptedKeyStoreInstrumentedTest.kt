/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.keyStore

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.util.Constants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.spongycastle.jce.ECNamedCurveTable
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PrivateKey

@RunWith(AndroidJUnit4ClassRunner::class)
class EncryptedKeyStoreInstrumentedTest {

    private val keyStore: EncryptedKeyStore
    private val keyRef: String = "TestKeys"
    private var expectedPrivateKey: PrivateKey

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        keyStore = EncryptedKeyStore(context)

        val secret = ByteArray(32) { pos -> pos.toByte() }
        val keySpec = org.spongycastle.jce.spec.ECPrivateKeySpec(BigInteger(1, secret), ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC))
        expectedPrivateKey = KeyFactory.getInstance("EC", "SC").generatePrivate(keySpec)
    }

    @Test
    fun saveAndGetPrivateKeyTest() {
        keyStore.storeKey(expectedPrivateKey, keyRef)
        val actualPrivateKey = keyStore.getKey<PrivateKey>(keyRef)
        assertThat(expectedPrivateKey).isEqualToComparingFieldByFieldRecursively(actualPrivateKey)
    }

}