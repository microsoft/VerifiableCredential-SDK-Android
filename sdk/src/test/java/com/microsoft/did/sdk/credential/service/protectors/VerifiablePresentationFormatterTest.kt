package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.serializer.Serializer
import com.nhaarman.mockitokotlin2.eq
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class VerifiablePresentationFormatterTest {

    @Mock
    lateinit var mockedTokenSigner : TokenSigner
    @Mock
    lateinit var mockedVerifiableCredential: VerifiableCredential

    lateinit var formatter: VerifiablePresentationFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val did: String = "did:test:2354543"
    private val audience: String = "audience2432"
    private val mockedRawVc: String = "raw24237"
    private val expiresIn: Int = 42
    private val serializer: Serializer =
        Serializer()
    private val mockedIdentifier = Identifier(
        did,
        "",
        signingKeyRef,
        "",
        "",
        "",
        "",
        ""
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        formatter = VerifiablePresentationFormatter(serializer, mockedTokenSigner)
        `when`<String>(mockedTokenSigner.signWithIdentifier(ArgumentMatchers.anyString(), eq(mockedIdentifier))).thenAnswer { it.arguments[0] }
        `when`<String>(mockedVerifiableCredential.raw).thenReturn(mockedRawVc)
    }

    @Test
    fun `create presentation`() {
        val verifiableCredentialList = listOf(mockedVerifiableCredential)
        val results = formatter.createPresentation(verifiableCredentialList, audience, mockedIdentifier, expiresIn)
        val contents = serializer.parse(VerifiablePresentationContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(did, contents.iss)
        assertEquals(listOf(Constants.VP_CONTEXT_URL), contents.vp.context)
        assertEquals(listOf(Constants.VERIFIABLE_PRESENTATION_TYPE), contents.vp.type)
        assertEquals(listOf(mockedRawVc), contents.vp.verifiableCredential)
    }
}