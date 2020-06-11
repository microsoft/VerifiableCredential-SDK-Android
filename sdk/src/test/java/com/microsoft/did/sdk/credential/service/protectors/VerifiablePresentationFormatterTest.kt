package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test
import kotlin.test.assertEquals

class VerifiablePresentationFormatterTest {

    private val mockedTokenSigner : TokenSigner = mockk()
    private val mockedVerifiableCredential: VerifiableCredential = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val slot = slot<String>()
    private val serializer: Serializer = Serializer()

    private val formatter: VerifiablePresentationFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedAudience: String = "audience2432"
    private val expectedRawVerifiableCredential: String = "raw24237"
    private val expectedExpiry: Int = 42
    private val expectedPresentationContext = listOf(Constants.VP_CONTEXT_URL)
    private val expectedPresentationType = listOf(Constants.VERIFIABLE_PRESENTATION_TYPE)

    init {
        formatter = VerifiablePresentationFormatter(serializer, mockedTokenSigner)
        every { mockedIdentifier.id } returns expectedDid
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedTokenSigner.signWithIdentifier(capture(slot), eq(mockedIdentifier)) } answers { slot.captured }
        every { mockedVerifiableCredential.raw } returns expectedRawVerifiableCredential
    }

    @Test
    fun createPresentationTest() {
        val verifiableCredentialList = listOf(mockedVerifiableCredential)
        val results = formatter.createPresentation(verifiableCredentialList, expectedAudience, mockedIdentifier, expectedExpiry)
        val contents = serializer.parse(VerifiablePresentationContent.serializer(), results)
        assertEquals(expectedAudience, contents.aud)
        assertEquals(expectedDid, contents.iss)
        assertEquals(expectedPresentationContext, contents.vp.context)
        assertEquals(expectedPresentationType, contents.vp.type)
        assertEquals(listOf(expectedRawVerifiableCredential), contents.vp.verifiableCredential)
    }
}