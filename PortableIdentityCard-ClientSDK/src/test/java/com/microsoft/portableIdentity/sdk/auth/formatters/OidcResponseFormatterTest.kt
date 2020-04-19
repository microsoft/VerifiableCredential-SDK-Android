package com.microsoft.portableIdentity.sdk.auth.formatters

import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseFormatter
import com.microsoft.portableIdentity.sdk.auth.protectors.TokenSigner
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyContainer
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.TokenFormatterException
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OidcResponseFormatterTest {

    // MOCKS FOR RETRIEVING PUBLIC KEY.
    @Mock
    lateinit var mockedCryptoOperations: CryptoOperations
    @Mock
    lateinit var mockedKeyStore: KeyStore
    @Mock
    lateinit var mockedKeyContainer: KeyContainer<PublicKey>
    @Mock
    lateinit var mockedPublicKey: PublicKey
    @Mock
    lateinit var mockedJsonWebKey: JsonWebKey

    // MOCK FOR RETRIEVING DID.
    @Mock
    lateinit var mockedIdentifier: Identifier
    @Mock
    lateinit var mockedIdentifierDocument: IdentifierDocument

    @Mock
    lateinit var mockedTokenSigner : TokenSigner

    @Mock
    lateinit var mockedGenericResponse: Response

    @Mock
    lateinit var mockedPresentationResponse: PresentationResponse

    @Mock
    lateinit var mockedIssuanceResponse: IssuanceResponse

    lateinit var formatter: OidcResponseFormatter

    private val signingKeyRef = "sigKeyRef"
    private val did = "did:test:567812"
    private val audience = "audience"
    private val thumbprint = "thumbprint42"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        formatter = OidcResponseFormatter(mockedCryptoOperations, mockedTokenSigner)
        setUpGetPublicKey(signingKeyRef, thumbprint)
        setUpIdentifier(signingKeyRef, did)
    }

    private fun setUpGetPublicKey(signingKeyRef: String, thumbprint: String) {
        `when`<KeyStore>(mockedCryptoOperations.keyStore).thenReturn(mockedKeyStore)
        `when`<KeyContainer<PublicKey>>(mockedKeyStore.getPublicKey(signingKeyRef)).thenReturn(mockedKeyContainer)
        `when`<PublicKey>(mockedKeyContainer.getKey()).thenReturn(mockedPublicKey)
        `when`<String>(mockedPublicKey.getThumbprint(mockedCryptoOperations, Sha.Sha256)).thenReturn(thumbprint)
        `when`<JsonWebKey>(mockedPublicKey.toJWK()).thenReturn(mockedJsonWebKey)
    }

    private fun setUpIdentifier(signingKeyRef: String, did: String) {
        `when`<IdentifierDocument>(mockedIdentifier.document).thenReturn(mockedIdentifierDocument)
        `when`<String>(mockedIdentifierDocument.id).thenReturn(did)
        `when`<String>(mockedIdentifier.signatureKeyReference).thenReturn(signingKeyRef)
    }

    private fun setUpPresentationResponse(nonce: String, state: String, audience: String) {
        `when`<String>(mockedPresentationResponse.nonce).thenReturn(nonce)
        `when`<String>(mockedPresentationResponse.state).thenReturn(state)
        `when`<String>(mockedPresentationResponse.audience).thenReturn(audience)
    }

    private fun setUpIssuanceResponse(contractUrl: String, audience: String) {
        `when`<String>(mockedIssuanceResponse.contractUrl).thenReturn(contractUrl)
        `when`<String>(mockedIssuanceResponse.audience).thenReturn(audience)
    }

    @Test
    fun `throw Formatter Exception for wrong Response type`() {
        val results = formatter.formAndSignResponse(mockedGenericResponse, mockedIdentifier)
        val expectedException = TokenFormatterException("Response type does not match OidcResponse")
        assertEquals(expectedException.message, (results as Result.Failure).payload.message)
        assert(results.payload is TokenFormatterException)
    }

    @Test
    fun `format Presentation Response with no attestations`() {
        val nonce = "123456789876"
        val state = "mockedState"
        setUpPresentationResponse(nonce, state, audience)
        val results = formatter.formContents(mockedPresentationResponse, mockedIdentifier)
        assertEquals(audience, results.aud)
        assertEquals(state, results.state)
        assertEquals(nonce, results.nonce)
        assertEquals(did, results.did)
        assertNull(results.contract)
        assertNull(results.attestations)
    }

    @Test
    fun `format Issuance Response with no attestations`() {
        val contractUrl = "http://testcontract.com"
        setUpIssuanceResponse(contractUrl, audience)
        val results = formatter.formContents(mockedIssuanceResponse, mockedIdentifier)
        assertEquals(audience, results.aud)
        assertNull(results.state)
        assertNull(results.nonce)
        assertEquals(did, results.did)
        assertEquals(contractUrl, results.contract)
        assertNull(results.attestations)
    }
}