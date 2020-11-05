/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.AesKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaHashedKeyAlgorithm
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.util.stringToByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class AndroidSubtleInstrumentedTest {
    private val androidSubtle: AndroidSubtle
    private var cryptoKeyPair: CryptoKeyPair
    private val keyStore: AndroidKeyStore

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        keyStore = AndroidKeyStore(context, defaultTestSerializer)
        androidSubtle = AndroidSubtle(keyStore)
        val keyReference = "KeyReference1"
        cryptoKeyPair = androidSubtle.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
    }

    @Test
    fun generateKeyTest() {
        val suppliedAlgorithm = AesKeyGenParams(W3cCryptoApiConstants.AesCbc.value, 128u)
        val expectedKeyType = KeyType.Secret
        val expectedKeyUsage = KeyUsage.Sign
        val actualKey = androidSubtle.generateKey(suppliedAlgorithm, true, listOf(KeyUsage.Sign))
        assertThat(actualKey.type).isEqualTo(expectedKeyType)
        assertThat(actualKey.extractable).isTrue()
        assertThat(actualKey.usages.firstOrNull()).isEqualTo(expectedKeyUsage)
        assertThat(actualKey.algorithm).isEqualToComparingFieldByFieldRecursively(suppliedAlgorithm)
    }

    @Test
    fun generateKeyPairTest() {
        val expectedKeyType = KeyType.Private
        val expectedKeyUsage = KeyUsage.Sign
        val expectedAlgorithm = W3cCryptoApiConstants.EcDsa.value
        val actualPrivateKey = cryptoKeyPair.privateKey
        assertThat(actualPrivateKey.type).isEqualTo(expectedKeyType)
        assertThat(actualPrivateKey.extractable).isFalse()
        assertThat(actualPrivateKey.usages.firstOrNull()).isEqualTo(expectedKeyUsage)
        assertThat(actualPrivateKey.algorithm.name).isEqualToIgnoringCase(expectedAlgorithm)
    }

    @Test
    fun exportPublicKeyJwk() {
        val actualKey = androidSubtle.exportKeyJwk(cryptoKeyPair.publicKey)
        assertThat(actualKey.kty).isEqualTo(com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value)
        assertThat(actualKey.key_ops?.firstOrNull()).isEqualTo("verify")
    }

    @Test
    fun signAndVerifySignatureTest() {
        val keyReference = "KeyReference4"
        cryptoKeyPair = androidSubtle.generateKeyPair(
            EcdsaParams(
                hash = Sha.SHA256.algorithm,
                additionalParams = mapOf(
                    "namedCurve" to "P-256K",
                    "format" to "DER",
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )

        var signature = androidSubtle.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
        val verified = androidSubtle.verify(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.publicKey, signature, payload)
        assertThat(verified).isTrue()
    }

    @Test
    fun importKeyTest() {
        val keyReference = "KeyReference5"
        cryptoKeyPair = androidSubtle.generateKeyPair(
            RsaHashedKeyAlgorithm(
                modulusLength = 4096UL,
                publicExponent = 65537UL,
                hash = Sha.SHA256.algorithm,
                additionalParams = mapOf("KeyReference" to keyReference)
            ), true, listOf(KeyUsage.Sign)
        )
        val actualJwk = androidSubtle.exportKeyJwk(cryptoKeyPair.publicKey)

        actualJwk.alg = cryptoKeyPair.publicKey.algorithm.name
        val expectedAlgorithm = JwaCryptoConverter.jwaAlgToWebCrypto(cryptoKeyPair.publicKey.algorithm.name)
        val expectedKeyUsage = KeyUsage.Sign
        val expectedKeyType = KeyType.Public
        val actualCryptoKey = androidSubtle.importKey(KeyFormat.Jwk, actualJwk, expectedAlgorithm, false, listOf(KeyUsage.Sign))
        assertThat(actualCryptoKey.type).isEqualTo(expectedKeyType)
        assertThat(actualCryptoKey.extractable).isFalse()
        assertThat(actualCryptoKey.algorithm).isEqualToComparingFieldByFieldRecursively(expectedAlgorithm)
        assertThat(actualCryptoKey.usages.firstOrNull()).isEqualTo(expectedKeyUsage)
    }

    @Test
    fun digestTest() {
        val testString = "abc"
        val payload = stringToByteArray(testString)
        val expectedDigestHex =
            "DDAF35A193617ABACC417349AE20413112E6FA4E89A97EA20A9EEEE64B55D39A2192992A274FC1A836BA3C23A3FEEBBD454D4423643CE80E2A9AC94FA54CA49F"
        val actualDigest = androidSubtle.digest(Sha.SHA512.algorithm, payload)
        var actualDigestHex = ""
        for (b in actualDigest) {
            val st = String.format("%02X", b)
            actualDigestHex += st
        }
        assertThat(actualDigestHex).isEqualTo(expectedDigestHex)
    }

}