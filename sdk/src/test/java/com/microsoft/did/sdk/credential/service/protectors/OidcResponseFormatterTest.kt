package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
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
    private val mockedVerifiableCredential: VerifiableCredential = mockk()
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
    private val expectedPresentationMapping = mapOf(expectedPresentationKey to mockedVerifiableCredential)
    private val expectedSelfIssued = mapOf("test3423442" to "self-attested")
    private val expectedIdTokens = mapOf("test2332342" to "idToken")

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
                listOf(mockedVerifiableCredential),
                expectedPresentationAudience,
                mockedIdentifier,
                expectedExpiry
            )
        } returns expectedVerifiablePresentation
    }

    @Test
    fun formatSimpleSiopResponseTest() {
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            expiresIn = expectedExpiry
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
            expiresIn = expectedExpiry,
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
            expiresIn = expectedExpiry,
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
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            expiresIn = expectedExpiry,
            requestedIdTokens = expectedIdTokens
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(expectedIdTokens, actualTokenContents.attestations?.idTokens)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations?.selfIssued)
        assertNull(actualTokenContents.attestations?.presentations)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun formatIssuanceResponseWithSelfAttestedAttestationsTest() {
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            expiresIn = expectedExpiry,
            requestedSelfIssuedClaims = expectedSelfIssued
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(expectedSelfIssued, actualTokenContents.attestations?.selfIssued)
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
            expiresIn = expectedExpiry,
            requestedVcs = expectedPresentationMapping
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
        try {
            formatter.format(
                responder = mockedIdentifier,
                responseAudience = expectedResponseAudience,
                expiresIn = expectedExpiry,
                contract = expectedContract,
                requestedVcs = expectedPresentationMapping
            )
            fail("Should throw Exception")
        } catch (exception: Exception) {
            assertThat(exception).isInstanceOf(FormatterException::class.java)
        }
    }

    @Test
    fun formatIssuanceResponseWithAllAttestationsTest() {
        val results = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            presentationsAudience = expectedPresentationAudience,
            expiresIn = expectedExpiry,
            requestedVcs = expectedPresentationMapping,
            requestedSelfIssuedClaims = expectedSelfIssued,
            requestedIdTokens = expectedIdTokens
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(mapOf(expectedPresentationKey to expectedVerifiablePresentation), actualTokenContents.attestations?.presentations)
        assertEquals(expectedSelfIssued, actualTokenContents.attestations?.selfIssued)
        assertEquals(expectedIdTokens, actualTokenContents.attestations?.idTokens)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }
}