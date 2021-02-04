package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.nimbusds.jose.jwk.ECKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.Test
import kotlin.test.assertEquals

class TokenSignerTest {

    // mocks for retrieving kid.
    private val mockedKeyStore: EncryptedKeyStore = mockk()
    private val mockKey: ECKey = mockk()
    private val mockedIdentifier: Identifier = mockk()

    private val signer: TokenSigner

    // TODO random numbers for these strings.
    private val signingKeyRef = "sigKeyRef5237"
    private val expectedDid = "did:test:567812"
    private val expectedKid = "#kid1426"
    private val expectedPayload = "payload12423"
    private val expectedTypeInJwtHeader = "JWT"
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
        every { mockedKeyStore.getKey(signingKeyRef) } answers { mockKey }
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