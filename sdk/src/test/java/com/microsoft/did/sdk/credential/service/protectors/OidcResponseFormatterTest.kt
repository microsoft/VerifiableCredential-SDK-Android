package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.RequestedIdTokenMap
import com.microsoft.did.sdk.credential.service.RequestedSelfAttestedClaimMap
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OidcResponseFormatterTest {

    // mocks for retrieving public key.
    private val mockedCryptoOperations: CryptoOperations = mockk()
    private val mockedKeyStore: KeyStore = mockk()
    private val mockedKeyContainer: KeyContainer<PublicKey> = mockk()
    private val mockedPublicKey: PublicKey = mockk()

    private val mockedTokenSigner: TokenSigner = mockk()
    private val slot = slot<String>()
    private val mockedVerifiablePresentationFormatter: VerifiablePresentationFormatter = mockk()
    private val mockedVc: VerifiableCredential = mockk()
    private val mockedVch: VerifiableCredentialHolder = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val serializer: Serializer = Serializer()

    private var formatter: OidcResponseFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedValidityInterval: Int = 32958
    private val expectedContract = "http://testcontract.com"
    private val expectedResponseAudience: String = "audience2432"
    private val expectedPresentationAudience: String = "audience6237"
    private val expectedThumbprint: String = "thumbprint534233"
    private val expectedExpiry: Int = 42
    private val expectedJsonWebKey = JsonWebKey()
    private val expectedVerifiablePresentation = "expectedPresentation"
    private val expectedSelfAttestedField = "testField3423442"
    private val expectedIdTokenConfig = "testIdTokenConfig234"
    private val expectedCredentialType: String = "type235"

    private val revocationRequest: RevocationRequest = mockk()
    private val mockedVcRaw = "testRawVc"
    private val expectedRevocationAudience = "audience1234"
    private val expectedRevokedRps = listOf("did:ion:test")
    private val expectedRevocationReason = "testing revocation"

    init {
        formatter = OidcResponseFormatter(
            mockedCryptoOperations,
            serializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner
        )
        setUpGetPublicKey()
        setUpExpectedPresentations()
        every { mockedTokenSigner.signWithIdentifier(capture(slot), mockedIdentifier) } answers { slot.captured }
        every {
            mockedVerifiablePresentationFormatter.createPresentation(
                mockedVc,
                expectedValidityInterval,
                expectedPresentationAudience,
                mockedIdentifier
            )
        } returns expectedVerifiablePresentation
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
                mockedVc,
                expectedValidityInterval,
                expectedPresentationAudience,
                mockedIdentifier
            )
        } returns expectedVerifiablePresentation
        every { mockedVch.verifiableCredential } returns mockedVc
    }

    @Test
    fun `format simple siop response`() {
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
    fun `format presentation response with no attestations`() {
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
    fun `format issuance response with no attestations`() {
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
    fun `format issuance response with id-token attestations`() {
        val expectedRawToken = "rawToken2343"
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            expiryInSeconds = expectedExpiry,
            requestedIdTokenMap = mapOf(expectedIdTokenConfig to expectedRawToken) as RequestedIdTokenMap
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
    fun `format issuance response with self attested attestations`() {
        val claimValue = "value5234"
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            expiryInSeconds = expectedExpiry,
            requestedSelfAttestedClaimMap = mapOf(expectedSelfAttestedField to claimValue) as RequestedSelfAttestedClaimMap
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(expectedSelfAttestedField, actualTokenContents.attestations?.selfIssued?.entries?.first()?.key)
        assertEquals(claimValue, actualTokenContents.attestations?.selfIssued?.entries?.first()?.value)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations?.idTokens)
        assertNull(actualTokenContents.attestations?.presentations)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun `format issuance response with presentation attestations`() {
        val mockedPresentationAttestation: PresentationAttestation = mockk()
        val mockedRequestedVchMap: RequestedVchMap = mutableMapOf(mockedPresentationAttestation to mockedVch)
        every { mockedPresentationAttestation.credentialType } returns expectedCredentialType
        every { mockedPresentationAttestation.validityInterval } returns expectedValidityInterval
        val actualFormattedToken = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            presentationsAudience = expectedPresentationAudience,
            contract = expectedContract,
            expiryInSeconds = expectedExpiry,
            requestedVchMap = mockedRequestedVchMap
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), actualFormattedToken)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(mapOf(expectedCredentialType to expectedVerifiablePresentation), actualTokenContents.attestations?.presentations)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.attestations?.idTokens)
        assertNull(actualTokenContents.attestations?.selfIssued)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun `format issuance response with all attestations`() {
        val expectedSelfAttestedValue = "value42938"
        val expectedRawToken = "rawToken2343"
        val mockedPresentationAttestation: PresentationAttestation = mockk()
        every { mockedPresentationAttestation.credentialType } returns expectedCredentialType
        every { mockedPresentationAttestation.validityInterval } returns expectedValidityInterval
        val mockedRequestedVchMap: RequestedVchMap = mutableMapOf(mockedPresentationAttestation to mockedVch)
        val results = formatter.format(
            responder = mockedIdentifier,
            responseAudience = expectedResponseAudience,
            contract = expectedContract,
            presentationsAudience = expectedPresentationAudience,
            expiryInSeconds = expectedExpiry,
            requestedVchMap = mockedRequestedVchMap,
            requestedSelfAttestedClaimMap = mapOf(expectedSelfAttestedField to expectedSelfAttestedValue) as RequestedSelfAttestedClaimMap,
            requestedIdTokenMap = mapOf(expectedIdTokenConfig to expectedRawToken) as RequestedIdTokenMap
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(expectedResponseAudience, actualTokenContents.aud)
        assertEquals(expectedContract, actualTokenContents.contract)
        assertEquals(expectedDid, actualTokenContents.did)
        assertEquals(expectedThumbprint, actualTokenContents.sub)
        assertEquals(expectedJsonWebKey, actualTokenContents.subJwk)
        assertEquals(mapOf(expectedCredentialType to expectedVerifiablePresentation), actualTokenContents.attestations?.presentations)
        assertEquals(expectedSelfAttestedField, actualTokenContents.attestations?.selfIssued?.entries?.first()?.key)
        assertEquals(expectedSelfAttestedValue, actualTokenContents.attestations?.selfIssued?.entries?.first()?.value)
        assertEquals(expectedRawToken, actualTokenContents.attestations?.idTokens?.entries?.first()?.value)
        assertEquals(expectedIdTokenConfig, actualTokenContents.attestations?.idTokens?.entries?.first()?.key)
        assertNull(actualTokenContents.nonce)
        assertNull(actualTokenContents.state)
        assertNull(actualTokenContents.vc)
        assertNull(actualTokenContents.recipient)
    }

    @Test
    fun `format revocation request with revoked RPs and reason for revocation`() {
        every { revocationRequest.audience } returns expectedRevocationAudience
        every { revocationRequest.reason } returns expectedRevocationReason
        every { revocationRequest.rpList } returns expectedRevokedRps
        every { revocationRequest.verifiableCredential } returns mockedVc
        every { mockedVc.raw } returns mockedVcRaw
        val results = formatter.format(
            responder = mockedIdentifier,
            responseAudience = revocationRequest.audience,
            expiryInSeconds = Constants.DEFAULT_EXPIRATION_IN_SECONDS,
            transformingVerifiableCredential = revocationRequest.verifiableCredential,
            revocationRPs = revocationRequest.rpList,
            revocationReason = revocationRequest.reason
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), results)
        assertThat(actualTokenContents.did).isEqualTo(expectedDid)
        assertThat(actualTokenContents.vc).isEqualTo(mockedVcRaw)
        assertThat(actualTokenContents.aud).isEqualTo(expectedRevocationAudience)
        assertThat(actualTokenContents.rp).isEqualTo(expectedRevokedRps)
        assertThat(actualTokenContents.reason).isEqualTo(expectedRevocationReason)
    }

    @Test
    fun `format revocation request with no reason and RP list`() {
        every { revocationRequest.audience } returns expectedRevocationAudience
        every { revocationRequest.verifiableCredential } returns mockedVc
        every { mockedVc.raw } returns mockedVcRaw
        val results = formatter.format(
            responder = mockedIdentifier,
            responseAudience = revocationRequest.audience,
            expiryInSeconds = Constants.DEFAULT_EXPIRATION_IN_SECONDS,
            transformingVerifiableCredential = revocationRequest.verifiableCredential
        )
        val actualTokenContents = serializer.parse(OidcResponseContent.serializer(), results)
        assertThat(actualTokenContents.did).isEqualTo(expectedDid)
        assertThat(actualTokenContents.vc).isEqualTo(mockedVcRaw)
        assertThat(actualTokenContents.aud).isEqualTo(expectedRevocationAudience)
        assertThat(actualTokenContents.rp).isNull()
        assertThat(actualTokenContents.reason).isNull()
    }
}