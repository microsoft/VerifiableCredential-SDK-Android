/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.crypto.keys

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class PairwiseKeyInstrumentedTest {
    private val androidSubtle: SubtleCrypto
    private val ellipticCurveSubtleCrypto: SubtleCrypto
    private val keyStore: AndroidKeyStore
    private var crypto: CryptoOperations
    private val seedReference = "masterSeed"

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = Serializer()
        keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, serializer)
        crypto = CryptoOperations(androidSubtle, keyStore)
    }

    @Test
    fun generatePersonaMasterKeyTest() {
        val seed = SecretKey(
            JsonWebKey(
                kty = KeyType.Octets.value,
                k = Base64Url.encode(stringToByteArray("abcdefg"))
            )
        )
        keyStore.save(seedReference, seed)

        val pairwiseKey = PairwiseKey(crypto)
        var masterKey = pairwiseKey.generatePersonaMasterKey(seedReference, "persona")
        var encodedMasterKey = Base64Url.encode(masterKey)
        assertThat(encodedMasterKey).isEqualTo("h-Z5gO1eBjY1EYXh64-f8qQF5ojeh1KVMKxmd0JI3YKScTOYjVm-h1j2pUNV8q6s8yphAR4lk5yXYiQhAOVlUw")
        masterKey = pairwiseKey.generatePersonaMasterKey(seedReference, "persona")
        encodedMasterKey = Base64Url.encode(masterKey)
        assertThat(encodedMasterKey).isEqualTo("h-Z5gO1eBjY1EYXh64-f8qQF5ojeh1KVMKxmd0JI3YKScTOYjVm-h1j2pUNV8q6s8yphAR4lk5yXYiQhAOVlUw")
        masterKey = pairwiseKey.generatePersonaMasterKey(seedReference, "persona1")
        encodedMasterKey = Base64Url.encode(masterKey)
        assertThat(encodedMasterKey).isNotEqualTo("h-Z5gO1eBjY1EYXh64-f8qQF5ojeh1KVMKxmd0JI3YKScTOYjVm-h1j2pUNV8q6s8yphAR4lk5yXYiQhAOVlUw")
    }

    @Test
    fun generateDeterministicECPairwiseKey() {
        val seed = SecretKey(
            JsonWebKey(
                kty = KeyType.Octets.value,
                k = Base64Url.encode(stringToByteArray("abcdefg"))
            )
        )
        keyStore.save(seedReference, seed)
        val alg = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.Sha256
            )
        )
        val ecAlgorithm = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        // Generate key
        val pairwiseKey = PairwiseKey(crypto)
        val ecPairwiseKey = pairwiseKey.generatePairwiseKey(alg, seedReference, "did:persona", "did:peer");
        keyStore.save("key", ecPairwiseKey);
        val data = stringToByteArray("1234567890")
        crypto = CryptoOperations(ellipticCurveSubtleCrypto, keyStore)

        //Signing and verifying just to make sure it is successful. Verify doesn't return anything to make assertions on that. If verification fails, test would fail automatically.
        val signature = crypto.sign(data, "key", ecAlgorithm)
        val verify = crypto.verify(data, signature, "key", ecAlgorithm);
    }
}