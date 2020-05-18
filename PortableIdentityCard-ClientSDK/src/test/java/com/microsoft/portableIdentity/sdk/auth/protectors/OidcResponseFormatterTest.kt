package com.microsoft.portableIdentity.sdk.auth.formatters

import com.microsoft.portableIdentity.sdk.auth.protectors.OidcResponseFormatter
import com.microsoft.portableIdentity.sdk.auth.protectors.TokenSigner
import com.microsoft.portableIdentity.sdk.auth.protectors.VerifiablePresentationFormatter
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.auth.validators.JwsValidator
import com.microsoft.portableIdentity.sdk.auth.validators.OidcPresentationRequestValidator
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyContainer
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test
import org.mockito.Mockito.`when`
import kotlin.test.assertEquals

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
    lateinit var mockedVerifiablePresentationFormatter: VerifiablePresentationFormatter

    @Mock
    lateinit var mockedSerializer : Serializer

    lateinit var formatter: OidcResponseFormatter

    private val signingKeyRef = "sigKeyRef"
    private val did = "did:test:567812"
    private val audience = "audience"
    private val thumbprint = "thumbprint42"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        formatter = OidcResponseFormatter(mockedCryptoOperations,
            mockedSerializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner)
        `when`<String>(mockedTokenSigner.signWithIdentifier("", mockedIdentifier)).thenReturn("")
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
//
    private fun setUpIdentifier(signingKeyRef: String, did: String) {
        `when`<String>(mockedIdentifierDocument.id).thenReturn(did)
        `when`<String>(mockedIdentifier.signatureKeyReference).thenReturn(signingKeyRef)
    }

//    private fun setUpPresentationResponse(nonce: String, state: String, audience: String) {
//        `when`<String>(mockedPresentationResponse.nonce).thenReturn(nonce)
//        `when`<String>(mockedPresentationResponse.state).thenReturn(state)
//        `when`<String>(mockedPresentationResponse.audience).thenReturn(audience)
//    }
//
//    private fun setUpIssuanceResponse(contractUrl: String, audience: String) {
//        `when`<String>(mockedIssuanceResponse.contractUrl).thenReturn(contractUrl)
//        `when`<String>(mockedIssuanceResponse.audience).thenReturn(audience)
//    }

    @Test
    fun `Form Simple SIOP Response`() {
        // val results = formatter.format(mockedIdentifier, "testAudience", 43)

        val token = "eyJraWQiOiJkaWQ6aW9uOkVpQ2lGZGtsRnEzX2ZHUnYweVc2NHFWNWxwTWJIX3FwUmxOdnFxNnhWWl9VU1E_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRE5UWkdjR1I0YlRsSFptWm1RMmg2UVhNM1MydFJSSFEyVW1sMWJtNUVjRTlzUVhsRlJXNWpkVlI1UVNJc0luSmxZMjkyWlhKNVgydGxlU0k2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpTTldvMVQycE9jazVzY1RCRU4yOVNNbmczUW5CTUxYaHFUM0l5Wmtrd1RtczBNbWN6YVVFd01uZEpJaXdpZVNJNklraExlbXBtV21jd1IxUXhjakZFVFdsc1JFZDRValJTTkVoc2RtdG1ZVWRzTld4UFJISlFTMVZyYkRRaWZTd2ljbVZqYjNabGNubGZZMjl0YldsMGJXVnVkQ0k2SWtWcFJEVnJiekJSZFVoUllqVllPWGhHYm1KNk1HeFFOMUJMYjJ4SlQzRm1kRmQ2UkhsRFpVOXlObmd0WVhjaWZRLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUVZKSExXUnVUblZUVFVnd2NEbGpNakZtWmsxbGJ6UldSRTB0VHpaWE9YUmpWbEpYZDBKc2JFWkVObWNpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpTMlY1Y3lJNlczc2lhV1FpT2lKemFXZHVJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKWVkyd3pObUp4YVhaRVQyaFFhMnh6ZUhNdFNVWkVaWEJoTVdobWFYTlNNWFZOY1c5d1pGSTFhalpGSWl3aWVTSTZJbDlPVkVOZlEyOXFORmxOTWxwTVZXZHFSbEkxWlVwSldIcEZNa2x4YmtoaFJIaFVUV1ZOYUhkWlNrVWlmU3dpZFhOaFoyVWlPbHNpYjNCeklpd2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEjc2lnbiIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2SyJ9.eyJyZXNwb25zZV90eXBlIjoiaWR0b2tlbiIsInJlc3BvbnNlX21vZGUiOiJmb3JtX3Bvc3QiLCJjbGllbnRfaWQiOiJodHRwczovL2xvY2FsaG9zdDozMDAwL3JlcXVlc3QiLCJyZWRpcmVjdF91cmkiOiJodHRwczovL2xvY2FsaG9zdDozMDAwL3ZlcmlmeSIsInNjb3BlIjoib3BlbmlkIGRpZF9hdXRobiIsInN0YXRlIjoiQ2N5U2w5Q3YyMjZzUUEiLCJub25jZSI6IldQM2p5cDdudURHbm53IiwiYXR0ZXN0YXRpb25zIjp7InByZXNlbnRhdGlvbnMiOlt7InJlcXVpcmVkIjp0cnVlLCJjcmVkZW50aWFsVHlwZSI6Ildvb2Rncm92ZUlkZW50aXR5Q3JlZGVudGlhbCIsImNvbnRyYWN0cyI6WyJodHRwczovL3BvcnRhYmxlaWRlbnRpdHljYXJkcy5henVyZS1hcGkubmV0L3YxLjAvNTM2Mjc5ZjYtMTVjYy00NWYyLWJlMmQtNjFlMzUyYjUxZWVmL3BvcnRhYmxlSWRlbnRpdGllcy9jb250cmFjdHMvV29vZGdyb3ZlSWQiXX1dfSwiaXNzIjoiZGlkOmlvbjpFaUNpRmRrbEZxM19mR1J2MHlXNjRxVjVscE1iSF9xcFJsTnZxcTZ4VlpfVVNRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbEROVFpHY0dSNGJUbEhabVptUTJoNlFYTTNTMnRSUkhRMlVtbDFibTVFY0U5c1FYbEZSVzVqZFZSNVFTSXNJbkpsWTI5MlpYSjVYMnRsZVNJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKU05XbzFUMnBPY2s1c2NUQkVOMjlTTW5nM1FuQk1MWGhxVDNJeVpra3dUbXMwTW1jemFVRXdNbmRKSWl3aWVTSTZJa2hMZW1wbVdtY3dSMVF4Y2pGRVRXbHNSRWQ0VWpSU05FaHNkbXRtWVVkc05XeFBSSEpRUzFWcmJEUWlmU3dpY21WamIzWmxjbmxmWTI5dGJXbDBiV1Z1ZENJNklrVnBSRFZyYnpCUmRVaFJZalZZT1hoR2JtSjZNR3hRTjFCTGIyeEpUM0ZtZEZkNlJIbERaVTl5Tm5ndFlYY2lmUS5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFFWSkhMV1J1VG5WVFRVZ3djRGxqTWpGbVprMWxielJXUkUwdFR6WlhPWFJqVmxKWGQwSnNiRVpFTm1jaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqUzJWNWN5STZXM3NpYVdRaU9pSnphV2R1SWl3aWRIbHdaU0k2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSllZMnd6Tm1KeGFYWkVUMmhRYTJ4emVITXRTVVpFWlhCaE1XaG1hWE5TTVhWTmNXOXdaRkkxYWpaRklpd2llU0k2SWw5T1ZFTmZRMjlxTkZsTk1scE1WV2RxUmxJMVpVcEpXSHBGTWtseGJraGhSSGhVVFdWTmFIZFpTa1VpZlN3aWRYTmhaMlVpT2xzaWIzQnpJaXdpWVhWMGFDSXNJbWRsYm1WeVlXd2lYWDFkZlgxZGZRIiwicmVnaXN0cmF0aW9uIjp7ImNsaWVudF9uYW1lIjoiRGVjZW50cmFsaXplZCBJZGVudGl0eSBUZWFtIiwiY2xpZW50X3B1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISIsInRvc191cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L3Rvcy5odG1sIiwibG9nb191cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L2ltYWdlcy9kaWRfbG9nby5wbmcifX0.MEQCIB7lhzohaDKcY09qSkOKvfHq5edteSgk3OqotvFEQvTRAiBM-vVmzeL2E01ewD5SVWtdNK305taxYeoALHxjruriCw"

    }

//    @Test
//    fun `format Presentation Response with no attestations`() {
//        val nonce = "123456789876"
//        val state = "mockedState"
//        setUpPresentationResponse(nonce, state, audience)
//        val results = formatter.formContents(mockedPresentationResponse, mockedIdentifier)
//        assertEquals(audience, results.aud)
//        assertEquals(state, results.state)
//        assertEquals(nonce, results.nonce)
//        assertEquals(did, results.did)
//        assertNull(results.contract)
//        assertNull(results.attestations)
//    }
//
//    @Test
//    fun `format Issuance Response with no attestations`() {
//        val contractUrl = "http://testcontract.com"
//        setUpIssuanceResponse(contractUrl, audience)
//        val results = formatter.formContents(mockedIssuanceResponse, mockedIdentifier)
//        assertEquals(audience, results.aud)
//        assertNull(results.state)
//        assertNull(results.nonce)
//        assertEquals(did, results.did)
//        assertEquals(contractUrl, results.contract)
//        assertNull(results.attestations)
//    }
}