package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.jwk.ECKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TokenSignerTest {

    // mocks for retrieving kid.
    private val mockedCryptoOperations: CryptoOperations = mockk()
    private val mockedKeyStore: EncryptedKeyStore = mockk()
    private val mockKey: ECKey = mockk()
    private val mockedIdentifier: Identifier = mockk()

    private val slot = slot<Map<String, String>>()

    private val signer: TokenSigner

    // TODO random numbers for these strings.
    private val signingKeyRef = "sigKeyRef5237"
    private val expectedDid = "did:test:567812"
    private val expectedKid = "#kid1426"
    private val expectedAlg = "ES256K"
    private val expectedPayload = "payload12423"
    private val expectedTypeInJwtHeader = "JWT"
    private val expectedHeader = mapOf("kid" to "$expectedDid$expectedKid", "typ" to expectedTypeInJwtHeader)
    private val serializer = Json
    private val expectedSignedPayload = "signedPayload45236"

    init {
        signer = TokenSigner(mockedKeyStore)
        setUpIdentifier()
        mockkConstructor(JwsToken::class)
    }

    //
    private fun setUpIdentifier() {
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    @Test
    fun signPayloadTest() {
        every {
            anyConstructed<JwsToken>().sign(mockKey)
        } answers { }
        every { anyConstructed<JwsToken>().serialize() } returns expectedSignedPayload
        val actualSignedPayload = signer.signWithIdentifier(expectedPayload, mockedIdentifier)
        assertEquals(expectedSignedPayload, actualSignedPayload)
    }
}