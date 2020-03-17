// Copyright (c) Microsoft Corporation. All rights reserved
package com.microsoft.portableIdentity.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithms.AesKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.portableIdentity.sdk.utilities.ConsoleLogger
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class AndroidSubtleInstrumentedTest {
    private val logger: ILogger = ConsoleLogger()
    private val androidSubtle: AndroidSubtle
    private var cryptoKeyPair: CryptoKeyPair

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val keyStore = AndroidKeyStore(context, logger)
        androidSubtle = AndroidSubtle(keyStore, logger)
        val keyReference: String = "KeyReference1"
        cryptoKeyPair = androidSubtle.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.Sha256,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
    }

    @Test
    fun getKeyTest() {
        val keyReference: String = "KeyReference2"
        val expectedAlgorithm = AesKeyGenParams(W3cCryptoApiConstants.AesCbc.value, 128u)
        val cryptoKey = androidSubtle.generateKey(expectedAlgorithm, true, listOf(KeyUsage.Sign))
        assertThat(cryptoKey.type).isNotNull()
        assertThat(cryptoKey.type).isEqualTo(KeyType.Secret)
        assertThat(cryptoKey.usages.firstOrNull()).isEqualTo(KeyUsage.Sign)
        assertThat(cryptoKey.algorithm.name).isEqualTo(W3cCryptoApiConstants.AesCbc.value)
    }

    @Test
    fun getKeyPairTest() {
        assertThat(cryptoKeyPair.privateKey).isNotNull()
    }

    @Test
    fun exportPublicKeyJwk() {
        val actualKey = androidSubtle.exportKeyJwk(cryptoKeyPair.publicKey)
        assertThat(actualKey.kty).isEqualTo(com.microsoft.portableIdentity.sdk.crypto.keys.KeyType.EllipticCurve.value)
        assertThat(actualKey.key_ops?.firstOrNull()).isEqualTo("verify")
//        assertThat(actualKey.alg).isNotNull()
    }

    @Test
    fun signPayloadTest() {
        val keyReference: String = "KeyReference3"
        cryptoKeyPair = androidSubtle.generateKeyPair(EcdsaParams(
            hash =  Sha.Sha256,
            additionalParams = mapOf(
                "namedCurve" to "P-256K",
                "format" to "DER",
                "KeyReference" to keyReference
            )
        ), true, listOf(KeyUsage.Sign))
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )
        var signedPayload = androidSubtle.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
        assertThat(signedPayload).isNotNull()
    }

    @Test
    fun verifySignatureTest() {
        val keyReference: String = "KeyReference4"
        cryptoKeyPair = androidSubtle.generateKeyPair(EcdsaParams(
            hash =  Sha.Sha256,
            additionalParams = mapOf(
                "namedCurve" to "P-256K",
                "format" to "DER",
                "KeyReference" to keyReference
            )
        ), true, listOf(KeyUsage.Sign))
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )
        var signedPayload = androidSubtle.sign(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.privateKey, payload)
        val verified = androidSubtle.verify(cryptoKeyPair.privateKey.algorithm, cryptoKeyPair.publicKey, signedPayload, payload)
        assertThat(verified).isTrue()
    }

    @Test
    fun importKeyTest() {
        val keyReference: String = "KeyReference5"
        cryptoKeyPair = androidSubtle.generateKeyPair(
            RsaHashedKeyAlgorithm(
                modulusLength = 4096UL,
                publicExponent = 65537UL,
                hash = Sha.Sha256,
                additionalParams = mapOf("KeyReference" to keyReference)
            ), true, listOf(KeyUsage.Sign)
        )
/*        val actualJwk = JsonWebKey(
            kty = com.microsoft.did.sdk.crypto.keys.KeyType.RSA.value,
            alg = "RSA-OAEP",
            use = KeyUse.Signature.value,
            kid = "#key1",
            key_ops = listOf(KeyUsage.Verify.value)
        )*/
        val actualJwk = androidSubtle.exportKeyJwk(cryptoKeyPair.publicKey)
        actualJwk.alg = cryptoKeyPair.publicKey.algorithm.name
        val actualAlgorithm = JwaCryptoConverter.jwaAlgToWebCrypto(cryptoKeyPair.publicKey.algorithm.name, logger = logger)
        val actualCryptoKey = androidSubtle.importKey(KeyFormat.Jwk, actualJwk, actualAlgorithm, false, listOf(KeyUsage.Sign))
        assertThat(actualCryptoKey.type).isEqualTo(KeyType.Public)
    }

    @Test
    fun digestTest() {
        val payload = byteArrayOf(
            123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
            32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10,
            32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
            109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
            111, 116, 34, 58, 116, 114, 117, 101, 125
        )
        val actualDigest = androidSubtle.digest(Sha.Sha512, payload)
        assertThat(actualDigest).isNotNull()
    }
}