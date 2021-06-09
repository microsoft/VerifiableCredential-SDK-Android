// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.defaultTestSerializer
import com.nimbusds.jose.jwk.JWK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SidetreePayloadProcessorTest {

    private val sidetreePayloadProcessor = SidetreePayloadProcessor(SideTreeHelper(), defaultTestSerializer)

    private val signingJwk =
        JWK.parse("{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"kid\":\"signKey\",\"x\":\"dqEeSHC5KhsMSW_Zh8kBzQXB9HLgZqzBtmkAh-tAw4U\",\"y\":\"Yo_a4_sB2METsA9YRD6II_PjbHiWg4gwqQJiOxx4Suk\"}")
    private val recoverJwk =
        JWK.parse("{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"kid\":\"recoverKey\",\"x\":\"weF3fxy4XqvMLpqYBROUMNf9q7MWpazuw4J5dEvsOO0\",\"y\":\"vxRL166b7Nv66bLI6EocgdNzGtiD_k-vEluRrZcEf-k\"}")
    private val updateJwk =
        JWK.parse("{\"kty\":\"EC\",\"crv\":\"secp256k1\",\"kid\":\"updateKey\",\"x\":\"iK9EveBFpO_KltBpKJfvq2KA2da-_VmYOwhJCAk6pRM\",\"y\":\"45wg6vkIT4JXcwzEJBjEWsJWxWv9cLXhdOAA4CACb8k\"}")

    @Test
    fun `signing key is assigned to patchData correctly`() {
        val actualRegistrationPayload = sidetreePayloadProcessor.generateCreatePayload(signingJwk, recoverJwk, updateJwk)
        val actualSigningDoc = actualRegistrationPayload.patchData.patches.first().document.publicKeys.first()
        assertThat(actualSigningDoc.id).isEqualTo(signingJwk.keyID)
        assertThat(actualSigningDoc.publicKeyJwk).isEqualToComparingFieldByFieldRecursively(signingJwk)
        assertThat(actualSigningDoc.purpose.first()).isEqualTo(Constants.IDENTIFIER_PUBLIC_KEY_PURPOSE)
    }

    @Test
    fun `update commitment is properly calculated from updateJwk`() {
        val expectedCommitment = "EiCrZyAhnMP-VM-m-C1-6VrCmwIka6fR0yDXQ1WIbjzbzg"
        val actualRegistrationPayload = sidetreePayloadProcessor.generateCreatePayload(signingJwk, recoverJwk, updateJwk)
        assertThat(actualRegistrationPayload.patchData.nextUpdateCommitmentHash).isEqualTo(expectedCommitment)
    }

    @Test
    fun `recovery commitment is properly calculated from recoveryJwk`() {
        val expectedCommitment = "EiDSwd8T1Uzd0gRKQ0MuBUaxdKlM_1GIO-NNEXaiW-4AqQ"
        val actualRegistrationPayload = sidetreePayloadProcessor.generateCreatePayload(signingJwk, recoverJwk, updateJwk)
        assertThat(actualRegistrationPayload.suffixData.nextRecoveryCommitmentHash).isEqualTo(expectedCommitment)
    }

    @Test
    fun `patchDataHash is calculated properly`() {
        val expectedHash = "EiCdaGTGyYstbXlbC6zb7tQXJMDa7NTP3tXxcTri9OjIAA"
        val actualRegistrationPayload = sidetreePayloadProcessor.generateCreatePayload(signingJwk, recoverJwk, updateJwk)
        assertThat(actualRegistrationPayload.suffixData.patchDataHash).isEqualTo(expectedHash)
    }

}