// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier


import com.nimbusds.jose.jwk.JWK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SideTreeHelperTest {

    private val sideTreeHelper = SideTreeHelper()

    private val testJwk =
        "{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"kid\":\"signKey\",\"x\":\"dqEeSHC5KhsMSW_Zh8kBzQXB9HLgZqzBtmkAh-tAw4U\",\"y\":\"Yo_a4_sB2METsA9YRD6II_PjbHiWg4gwqQJiOxx4Suk\"}"
    private val testJwkRearrangedWithSpaces =
        "{ \"kid\" : \"signKey\",\"crv\":\"secp256k1\",\"kty\":\"EC\",\"x\":\"dqEeSHC5KhsMSW_Zh8kBzQXB9HLgZqzBtmkAh-tAw4U\",\"y\":\"Yo_a4_sB2METsA9YRD6II_PjbHiWg4gwqQJiOxx4Suk\"}"

    @Test
    fun `multi hash is equal through canonicalization`() {
        val actualMultiHash1 = sideTreeHelper.canonicalizeMultiHashEncode(testJwk)
        val actualMultiHash2 = sideTreeHelper.canonicalizeMultiHashEncode(testJwkRearrangedWithSpaces)
        assertThat(actualMultiHash1).isEqualTo(actualMultiHash2)
    }

    @Test
    fun `multi hash result`() {
        val expectedHash = "EiCoXB_oNrdSXnCDs4QZ7gjq0e986KfN_4WoHfAlo6iNyw"
        val actualHash = sideTreeHelper.canonicalizeMultiHashEncode(testJwkRearrangedWithSpaces)
        assertThat(actualHash).isEqualTo(expectedHash)
    }

    @Test
    fun `createCommitmentValue is equal through canonicalization`() {
        val actualCommitment1 = sideTreeHelper.createCommitmentValue(JWK.parse(testJwk))
        val actualCommitment2 = sideTreeHelper.createCommitmentValue(JWK.parse(testJwkRearrangedWithSpaces))
        assertThat(actualCommitment1).isEqualTo(actualCommitment2)
    }

    @Test
    fun `createCommitmentValue result`() {
        val expectedCommitment = "EiDGJuWSAbriN_z4zES82csqhfwN__DFLwFFbchNNKoucg"
        val actualCommitment1 = sideTreeHelper.createCommitmentValue(JWK.parse(testJwkRearrangedWithSpaces))
        assertThat(actualCommitment1).isEqualTo(expectedCommitment)
    }

}