package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import kotlinx.serialization.json.Json
import org.junit.Test

class JweTokenTest {

    @Test
    fun JweRfcA1() {
        val expectedPlaintext = "The true sign of intelligence is not knowledge but imagination."
        val token = JweToken(Json.Default, expectedPlaintext)
        token.
    }
}