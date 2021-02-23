// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.microsoft.did.sdk.util.Constants.AES_KEY
import org.junit.Test
import java.math.BigInteger
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import javax.crypto.spec.SecretKeySpec

class AlgorithmsTest {

    private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }

    @Test
    fun `test DigestAlgorithm Sha256`() {
        val actualDigest = CryptoOperations.digest("abcd".toByteArray(), DigestAlgorithm.Sha256)
        val expectedDigest = "88d4266fd4e6338d13b845fcf289579d209c897823b9217da3e161936f031589"
        assertThat(actualDigest.toHexString()).isEqualTo(expectedDigest)
    }

    @Test
    fun `test KeyGenAlgorithm Secp256k1`() {
        val actualKeys = CryptoOperations.generateKeyPair(KeyGenAlgorithm.Secp256k1)
        assertThat(actualKeys.private).isInstanceOf(ECPrivateKey::class)
        assertThat(actualKeys.public).isInstanceOf(ECPublicKey::class)
    }

    @Test
    fun `test MacAlgorithm HmacSha512`() {
        val seed = ByteArray(16, { it.toByte() })
        val secretKey = SecretKeySpec(seed, AES_KEY)
        val actualMac = CryptoOperations.computeMac("abcd".toByteArray(), secretKey, MacAlgorithm.HmacSha512)
        val expectedMac =
            "d5ab30f6bf979aa39519441d9a6821fb43387261006ab91a561c687ba24a823cc1f8cc7144591c0942435408074232252a9f098aa38b077c39cd18dc5effc1e9"
        assertThat(actualMac.toHexString()).isEqualTo(expectedMac)
    }

    @Test
    fun `test PrivateKeyFactoryAlgorithm Secp256k1`() {
        val expectedS = BigInteger("9764925054458648487875837691249734695986519782169248696406848767427475897986")
        val privateKey =
            CryptoOperations.generateKey<ECPrivateKey>(PrivateKeyFactoryAlgorithm.Secp256k1(expectedS))
        assertThat(privateKey).isInstanceOf(ECPrivateKey::class)
        assertThat(privateKey.s).isEqualTo(expectedS)
    }

    @Test
    fun `test PublicKeyFactoryAlgorithm Secp256k1`() {
        val expectedX = BigInteger("52821953784837859148297498991218118885836932354090668569542922319384208430148")
        val expectedY = BigInteger("115319546132531470209270578804017583676805014907014973682639068158056817576714")
        val publicKey = CryptoOperations.generateKey<ECPublicKey>(PublicKeyFactoryAlgorithm.Secp256k1(expectedX, expectedY))
        assertThat(publicKey.w.affineX).isEqualTo(expectedX)
        assertThat(publicKey.w.affineY).isEqualTo(expectedY)
    }
}