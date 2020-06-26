// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.serializer.Serializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class SidetreePayloadProcessorTest {
    val serializer = Serializer()
    val sidetreePayloadProcessor = SidetreePayloadProcessor(serializer)

    @Ignore
    @Test
    fun canonicalizeTestUsingTestVector() {
        val testJwk = JsonWebKey(
                kty="EC",
                crv="secp256k1",
                x="5s3-bKjD1Eu_3NJu8pk7qIdOPl1GBzU_V8aR3xiacoM",
                y="v0-Q5H3vcfAfQ4zsebJQvMrIg3pcsaJzRvuIYZ3_UOY"
        )

        val actualCanonicalizedByteArray = sidetreePayloadProcessor.canonicalizePublicKeyAsByteArray(testJwk)
        val expectedCanonicalizedString = """{"crv":"secp256k1","kty":"EC","x":"5s3-bKjD1Eu_3NJu8pk7qIdOPl1GBzU_V8aR3xiacoM","y":"v0-Q5H3vcfAfQ4zsebJQvMrIg3pcsaJzRvuIYZ3_UOY"}"""
        assertThat(byteArrayToString(actualCanonicalizedByteArray)).isEqualTo(expectedCanonicalizedString)
    }
}