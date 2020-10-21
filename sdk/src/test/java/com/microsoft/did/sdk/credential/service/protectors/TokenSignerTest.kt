package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
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
    private val mockedKeyStore: KeyStore = mockk()
    private val mockedKeyContainer: KeyContainer<PrivateKey> = mockk()
    private val mockedPrivateKey: PrivateKey = mockk()

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
        signer = TokenSigner(mockedCryptoOperations, serializer)
        setUpGetPrivateKey()
        setUpIdentifier()
        mockkConstructor(JwsToken::class)
    }

    private fun setUpGetPrivateKey() {
        every { mockedCryptoOperations.keyStore } returns mockedKeyStore
        every { mockedKeyStore.getPrivateKey(signingKeyRef) } returns mockedKeyContainer
        every { mockedKeyContainer.getKey() } returns mockedPrivateKey
        every { mockedPrivateKey.kid } returns expectedKid
        every { mockedPrivateKey.alg } returns expectedAlg
    }

    //
    private fun setUpIdentifier() {
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    @Test
    fun signPayloadTest() {
        every {
            anyConstructed<JwsToken>().sign(signingKeyRef, mockedCryptoOperations, capture(slot))
        } answers {
            assertEquals(expectedHeader, slot.captured)
        }
        every { anyConstructed<JwsToken>().serialize(serializer) } returns expectedSignedPayload
        val actualSignedPayload = signer.signWithIdentifier(expectedPayload, mockedIdentifier)
        assertEquals(expectedSignedPayload, actualSignedPayload)

    }
}