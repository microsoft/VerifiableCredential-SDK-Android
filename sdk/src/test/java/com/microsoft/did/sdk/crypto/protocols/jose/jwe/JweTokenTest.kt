package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class JweTokenTest {
    private val keyRef = "TestKeyID"
    private val key: ECKey = ECKeyGenerator(Curve.P_256)
        .keyID(keyRef)
        .generate()

    // RFC 7516 A.2
    private val expectedPlaintext = "Live long and prosper."
    private val expectedToken = "eyJhbGciOiJSU0ExXzUiLCJlbmMiOiJBMTI4Q0JDLUhTMjU2In0." +
        "UGhIOguC7IuEvf_NPVaXsGMoLOmwvc1GyqlIKOK1nN94nHPoltGRhWhw7Zx0-kFm" +
        "1NJn8LE9XShH59_i8J0PH5ZZyNfGy2xGdULU7sHNF6Gp2vPLgNZ__deLKxGHZ7Pc" +
        "HALUzoOegEI-8E66jX2E4zyJKx-YxzZIItRzC5hlRirb6Y5Cl_p-ko3YvkkysZIF" +
        "NPccxRU7qve1WYPxqbb2Yw8kZqa2rMWI5ng8OtvzlV7elprCbuPhcCdZ6XDP0_F8" +
        "rkXds2vE4X-ncOIM8hAYHHi29NX0mcKiRaD0-D-ljQTP-cFPgwCp6X-nZZd9OHBv" +
        "-B3oWh2TbqmScqXMR4gp_A." +
        "AxY8DCtDaGlsbGljb3RoZQ." +
        "KDlTtXchhZTGufMYmOYGS4HffxPSUrfmqCHXaI9wOGY." +
        "9hH0vgRfYgPnAHOd8stkvw"
    private val rsaKey = com.nimbusds.jose.jwk.RSAKey.parse(
        "{\"kty\":\"RSA\"," +
            "      \"n\":\"sXchDaQebHnPiGvyDOAT4saGEUetSyo9MKLOoWFsueri23bOdgWp4Dy1Wl" +
            "           UzewbgBHod5pcM9H95GQRV3JDXboIRROSBigeC5yjU1hGzHHyXss8UDpre" +
            "           cbAYxknTcQkhslANGRUZmdTOQ5qTRsLAt6BTYuyvVRdhS8exSZEy_c4gs_" +
            "           7svlJJQ4H9_NxsiIoLwAEk7-Q3UXERGYw_75IDrGA84-lA_-Ct4eTlXHBI" +
            "           Y2EaV7t7LjJaynVJCpkv4LKjTTAumiGUIuQhrNhZLuF_RJLqHpM2kgWFLU" +
            "           7-VTdL1VbC2tejvcI2BlMkEpk1BzBZI0KQB0GaDWFLN-aEAw3vRw\"," +
            "      \"e\":\"AQAB\"," +
            "      \"d\":\"VFCWOqXr8nvZNyaaJLXdnNPXZKRaWCjkU5Q2egQQpTBMwhprMzWzpR8Sxq" +
            "           1OPThh_J6MUD8Z35wky9b8eEO0pwNS8xlh1lOFRRBoNqDIKVOku0aZb-ry" +
            "           nq8cxjDTLZQ6Fz7jSjR1Klop-YKaUHc9GsEofQqYruPhzSA-QgajZGPbE_" +
            "           0ZaVDJHfyd7UUBUKunFMScbflYAAOYJqVIVwaYR5zWEEceUjNnTNo_CVSj" +
            "           -VvXLO5VZfCUAVLgW4dpf1SrtZjSt34YLsRarSb127reG_DUwg9Ch-Kyvj" +
            "           T1SkHgUWRVGcyly7uvVGRSDwsXypdrNinPA4jlhoNdizK2zF2CWQ\"," +
            "      \"p\":\"9gY2w6I6S6L0juEKsbeDAwpd9WMfgqFoeA9vEyEUuk4kLwBKcoe1x4HG68" +
            "           ik918hdDSE9vDQSccA3xXHOAFOPJ8R9EeIAbTi1VwBYnbTp87X-xcPWlEP" +
            "           krdoUKW60tgs1aNd_Nnc9LEVVPMS390zbFxt8TN_biaBgelNgbC95sM\"," +
            "      \"q\":\"uKlCKvKv_ZJMVcdIs5vVSU_6cPtYI1ljWytExV_skstvRSNi9r66jdd9-y" +
            "           BhVfuG4shsp2j7rGnIio901RBeHo6TPKWVVykPu1iYhQXw1jIABfw-MVsN" +
            "           -3bQ76WLdt2SDxsHs7q7zPyUyHXmps7ycZ5c72wGkUwNOjYelmkiNS0\"," +
            "      \"dp\":\"w0kZbV63cVRvVX6yk3C8cMxo2qCM4Y8nsq1lmMSYhG4EcL6FWbX5h9yuv" +
            "           ngs4iLEFk6eALoUS4vIWEwcL4txw9LsWH_zKI-hwoReoP77cOdSL4AVcra" +
            "           Hawlkpyd2TWjE5evgbhWtOxnZee3cXJBkAi64Ik6jZxbvk-RR3pEhnCs\"," +
            "      \"dq\":\"o_8V14SezckO6CNLKs_btPdFiO9_kC1DsuUTd2LAfIIVeMZ7jn1Gus_Ff" +
            "           7B7IVx3p5KuBGOVF8L-qifLb6nQnLysgHDh132NDioZkhH7mI7hPG-PYE_" +
            "           odApKdnqECHWw0J-F0JWnUd6D2B_1TvF9mXA2Qx-iGYn8OVV1Bsmp6qU\"," +
            "      \"qi\":\"eNho5yRBEBxhGBtQRww9QirZsB66TrfFReG_CcteI1aCneT0ELGhYlRlC" +
            "           tUkTRclIfuEPmNsNDPbLoLqqCVznFbvdB7x-Tl-m0l_eFTj2KiqwGqE9PZ" +
            "           B9nNTwMVvH3VRRSLWACvPnSiwP8N5Usy-WRXS-V7TbpxIhvepTfE0NNo\"}"
    )

    @Test
    fun `deserialize and decrypt test`() {
        val token = JweToken.deserialize(expectedToken)
        assertEquals(JWEAlgorithm.RSA1_5, token.getKeyAlgorithm(), "Expected algorithm to match RFC")
        val plaintext = token.decrypt(null, rsaKey.toRSAPrivateKey())
        assertNotNull(plaintext, "failed to decrypt")
        assertEquals(expectedPlaintext, String(plaintext), "plaintext does not match")
        assertEquals(plaintext, token.contentAsByteArray, "should be able to retrieve payload later")
        assertEquals(expectedPlaintext, token.contentAsString, "plaintext does not match")
    }

    @Test
    fun `decrypt requires arguments`() {
        val token = JweToken.deserialize(expectedToken)
        assertFailsWith(IllegalArgumentException::class, "thrown exception was not expected") {
            token.decrypt(null, null)
        }
    }

    @Test
    fun `decrypt bad keys throw`() {
        val token = JweToken.deserialize(expectedToken)
        assertFailsWith(KeyException::class) {
            token.decrypt(null, key.toPrivateKey())
        }
    }

    @Test
    fun `encrypt and serialize Test`() {
        val token = JweToken(expectedPlaintext)
        token.encrypt(
            key.toPublicJWK(),
            JWEHeader.Builder(JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.A256CBC_HS512)
                .keyID(keyRef)
                .build()
        )
        val jwe = token.serialize()
        val decryptedToken = JweToken.deserialize(jwe)
        val keystore: EncryptedKeyStore = mockk()
        every { keystore.getKey(keyRef) } returns (key)
        val decrypted = decryptedToken.decrypt(keystore)
        assertNotNull(decrypted, "Expected token to decrypt")
        assertEquals(expectedPlaintext, String(decrypted), "Does not match expected payload")
        assertEquals(expectedPlaintext, decryptedToken.contentAsString, "does not match expected payload")
    }

}