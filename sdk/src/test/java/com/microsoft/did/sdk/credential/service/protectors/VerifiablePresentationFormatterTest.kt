package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class VerifiablePresentationFormatterTest {

    private val mockedTokenSigner: TokenSigner = mockk()
    private val mockedVerifiableCredential: VerifiableCredential = mockk()
    private val mockedPresentationAttestation: PresentationAttestation = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val slot = slot<String>()
    private val serializer: Json = Json

    private val formatter: VerifiablePresentationFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedAudience: String = "audience2432"
    private val expectedRawVerifiableCredential: String = "raw24237"
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
    fun `create presentation`() {
        val expectedValidityInterval = 2343
        every { mockedPresentationAttestation.validityInterval } returns expectedValidityInterval
        val results = formatter.createPresentation(mockedVerifiableCredential, expectedValidityInterval, expectedAudience, mockedIdentifier)
        val contents = serializer.decodeFromString(VerifiablePresentationContent.serializer(), results)
        assertEquals(expectedAudience, contents.audience)
        assertEquals(expectedDid, contents.issuerOfVp)
        assertEquals(expectedPresentationContext, contents.verifiablePresentation.context)
        assertEquals(expectedPresentationType, contents.verifiablePresentation.type)
        assertEquals(listOf(expectedRawVerifiableCredential), contents.verifiablePresentation.verifiableCredential)
    }
}