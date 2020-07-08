package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.contexts.IdTokenContext
import com.microsoft.did.sdk.credential.service.models.contexts.SelfAttestedClaimContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiableCredentialContext
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.FormatterException
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class OidcResponseFormatterTest {

    // mocks for retrieving public key.
    private val mockedCryptoOperations: CryptoOperations = mockk()
    private val mockedKeyStore: KeyStore  = mockk()
    private val mockedKeyContainer: KeyContainer<PublicKey> = mockk()
    private val mockedPublicKey: PublicKey = mockk()

    private val mockedTokenSigner: TokenSigner = mockk()
    private val slot = slot<String>()
    private val mockedVerifiablePresentationFormatter: VerifiablePresentationFormatter = mockk()
    private val mockedVerifiableCredentialContext: VerifiableCredentialContext = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val serializer: Serializer = Serializer()

    private var formatter: OidcResponseFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedContract = "http://testcontract.com"
    private val expectedResponseAudience: String = "audience2432"
    private val expectedPresentationAudience: String = "audience6237"
    private val expectedThumbprint: String = "thumbprint534233"
    private val expectedExpiry: Int = 42
    private val expectedJsonWebKey = JsonWebKey()
    private val expectedPresentationKey = "test543534"
    private val expectedVerifiablePresentation = "expectedPresentation"
    private val expectedSelfAttestedField = "testField3423442"
    private val expectedIdTokenConfig = "testIdTokenConfig234"

    init {
        formatter = OidcResponseFormatter(
            mockedCryptoOperations,
            serializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner
        )
        setUpGetPublicKey()
        setUpExpectedPresentations()
        every { mockedTokenSigner.signWithIdentifier(capture(slot), mockedIdentifier)} answers { slot.captured }
        every { mockedVerifiablePresentationFormatter.createPresentation(
            mockedVerifiableCredentialContext,
            expectedPresentationAudience,
            mockedIdentifier
        ) } returns expectedVerifiablePresentation
    }

    private fun setUpGetPublicKey() {
        every { mockedIdentifier.signatureKeyReference } returns signingKeyRef
        every { mockedIdentifier.id } returns expectedDid
        every { mockedCryptoOperations.keyStore } returns mockedKeyStore
        every { mockedKeyStore.getPublicKey(signingKeyRef) } returns mockedKeyContainer
        every { mockedKeyContainer.getKey() } returns mockedPublicKey
        every { mockedPublicKey.getThumbprint(mockedCryptoOperations, Sha.SHA256.algorithm) } returns expectedThumbprint
        every { mockedPublicKey.toJWK() } returns expectedJsonWebKey
    }

    private fun setUpExpectedPresentations() {
        every {
            mockedVerifiablePresentationFormatter.createPresentation(
                mockedVerifiableCredentialContext,
                expectedPresentationAudience,
                mockedIdentifier
            )
        } returns expectedVerifiablePresentation
    }

    @Test
    fun formatSimpleSiopResponseTest() {
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            expiryInSeconds = expectedExpiry
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertNull(actualTokenContents.contract)
        assertNull(actualTokenContents.attestations)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.nonce)
    }

    @Test
    fun formatPresentationResponseWithNoAttestationsTest() {
        val nonce = "123456789876"
        val state = "mockedState"
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            expiryInSeconds = expectedExpiry,
            nonce = nonce,
            state = state
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(state, actualTokenContents.state)
        assertEquals(nonce, actualTokenContents.nonce)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertNull(actualTokenContents.contract)
        assertNull(actualTokenContents.attestations)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun formatIssuanceResponseWithNoAttestationsTest() {
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            expiryInSeconds = expectedExpiry,
            contract = expectedContract
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun formatIssuanceResponseWithIdTokenAttestationsTest() {
        val expectedIdTokenContext: IdTokenContext = mockk()
        val expectedRawToken = "rawToken2343"
        every { expectedIdTokenContext.rawToken } returns expectedRawToken
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            expiryInSeconds = expectedExpiry,
            idTokenContexts = mapOf(expectedIdTokenConfig to expectedIdTokenContext)
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(expectedRawToken, actualTokenContents.attestations?.idTokens?.entries?.first()?.value)
        assertEquals(expectedIdTokenConfig, actualTokenContents.attestations?.idTokens?.entries?.first()?.key)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations?.selfIssued)
        assertNull(actualTokenContents.attestations?.presentations)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun formatIssuanceResponseWithSelfAttestedAttestationsTest() {
        val expectedSelfAttestedClaimContext: SelfAttestedClaimContext = mockk()
        val contextValue = "value5234"
        every { expectedSelfAttestedClaimContext.value } returns contextValue
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            expiryInSeconds = expectedExpiry,
            selfAttestedClaimContexts = mapOf(expectedSelfAttestedField to expectedSelfAttestedClaimContext)
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(expectedSelfAttestedField, actualTokenContents.attestations?.selfIssued?.entries?.first()?.key)
        assertEquals(contextValue, actualTokenContents.attestations?.selfIssued?.entries?.first()?.value)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations?.idTokens)
        assertNull(actualTokenContents.attestations?.presentations)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun formatIssuanceResponseWithPresentationAttestationsTest() {
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            presentationsAudience = expectedPresentationAudience,
            contract = expectedContract,
            expiryInSeconds = expectedExpiry,
            verifiableCredentialContexts = mapOf(expectedPresentationKey to mockedVerifiableCredentialContext)
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(mapOf(expectedPresentationKey to expectedVerifiablePresentation), actualTokenContents.attestations?.presentations)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations?.idTokens)
        assertNull(actualTokenContents.attestations?.selfIssued)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun formatIssuanceResponseWithPresentationAttestationsAndNoPresentationAudienceTest() {
        val verifiableCredentialContext: VerifiableCredentialContext = mockk()
        try {
            formatter.format(
                responder = mockedIdentifier,
                responseAudience = expectedResponseAudience,
                expiryInSeconds = expectedExpiry,
                contract = expectedContract,
                verifiableCredentialContexts = mapOf(expectedPresentationKey to verifiableCredentialContext)
            )
            fail("Should throw Exception")
        } catch (exception: Exception) {
            assertThat(exception).isInstanceOf(FormatterException::class.java)
        }
    }

    @Test
    fun formatIssuanceResponseWithAllAttestationsTest() {
        val expectedSelfAttestedClaimContext: SelfAttestedClaimContext = mockk()
        val expectedSelfAttestedValue = "value42938"
        every { expectedSelfAttestedClaimContext.value } returns expectedSelfAttestedValue
        val expectedIdTokenContext: IdTokenContext = mockk()
        val expectedRawToken = "rawToken2343"
        every { expectedIdTokenContext.rawToken } returns expectedRawToken
        val results = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            presentationsAudience = expectedPresentationAudience,
            expiryInSeconds = expectedExpiry,
            verifiableCredentialContexts = mapOf(expectedPresentationKey to mockedVerifiableCredentialContext),
            selfAttestedClaimContexts = mapOf(expectedSelfAttestedField to expectedSelfAttestedClaimContext),
            idTokenContexts = mapOf(expectedIdTokenConfig to expectedIdTokenContext)
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(mapOf(expectedPresentationKey to expectedVerifiablePresentation), actualTokenContents.attestations?.presentations)
        assertEquals(expectedSelfAttestedField, actualTokenContents.attestations?.selfIssued?.entries?.first()?.key)
        assertEquals(expectedSelfAttestedValue, actualTokenContents.attestations?.selfIssued?.entries?.first()?.value)
        assertEquals(expectedRawToken, actualTokenContents.attestations?.idTokens?.entries?.first()?.value)
        assertEquals(expectedIdTokenConfig, actualTokenContents.attestations?.idTokens?.entries?.first()?.key)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }
}