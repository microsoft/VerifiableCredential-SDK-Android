package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyContainer
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
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

    // MOCK FOR RETRIEVING DID.
    @Mock
    lateinit var mockedIdentifier: Identifier

    @Mock
    lateinit var mockedTokenSigner : TokenSigner
    @Mock
    lateinit var mockedVerifiablePresentationFormatter: VerifiablePresentationFormatter
    @Mock
    lateinit var mockedVerifiableCredential: VerifiableCredential

    lateinit var formatter: OidcResponseFormatter

    private lateinit var signingKeyRef: String
    private lateinit var did: String
    private lateinit var audience: String
    private lateinit var thumbprint: String
    private var expiresIn: Int = 42
    private val serializer: Serializer = Serializer()
    private val mockedJsonWebKey = JsonWebKey()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        formatter = OidcResponseFormatter(mockedCryptoOperations,
            serializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner)
        randomizeStaticParams()
        setUpGetPublicKey(signingKeyRef, thumbprint)
        setUpIdentifier(signingKeyRef, did)
        `when`<String>(mockedTokenSigner.signWithIdentifier(ArgumentMatchers.anyString(), com.nhaarman.mockitokotlin2.eq(mockedIdentifier))).thenAnswer { it.arguments[0] }
    }

    private fun randomizeStaticParams() {
        signingKeyRef =  "sigKeyRef1243523"
        did =  "did:test:2354543"
        audience = "audience2432"
        thumbprint = "thumbprint534233"
        expiresIn = 42
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
        `when`<String>(mockedIdentifier.id).thenReturn(did)
        `when`<String>(mockedIdentifier.signatureKeyReference).thenReturn(signingKeyRef)
    }

    @Test
    fun `Form Simple SIOP Response`() {
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertNull(contents.contract)
        assertNull(contents.attestations)
        assertNull(contents.state)
        assertNull(contents.nonce)
    }

    @Test
    fun `format Presentation Response with no attestations`() {
        val nonce = "123456789876"
        val state = "mockedState"
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            nonce = nonce,
            state = state)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(state, contents.state)
        assertEquals(nonce, contents.nonce)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertNull(contents.contract)
        assertNull(contents.attestations)
        assertNull(contents.vc)
        assertNull(contents.recipient)
    }

    @Test
    fun `format Issuance Response with no attestations`() {
        val contractUrl = "http://testcontract.com"
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            contract = contractUrl)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(contractUrl, contents.contract)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertNull(contents.nonce)
        assertNull(contents.state)
        assertNull(contents.attestations)
        assertNull(contents.vc)
        assertNull(contents.recipient)
    }

    @Test
    fun `format Issuance Response with idToken attestations`() {
        val requestedIdTokens = mapOf("test235445" to "idToken")
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedIdTokens = requestedIdTokens)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertEquals(requestedIdTokens, contents.attestations?.idTokens)
        assertNull(contents.nonce)
        assertNull(contents.state)
        assertNull(contents.attestations?.selfIssued)
        assertNull(contents.attestations?.presentations)
        assertNull(contents.vc)
        assertNull(contents.recipient)
    }

    @Test
    fun `format Issuance Response with self-attested attestations`() {
        val requestedSelfIssued = mapOf("test3342" to "self-attested")
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedSelfIssuedClaims = requestedSelfIssued)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertEquals(requestedSelfIssued, contents.attestations?.selfIssued)
        assertNull(contents.nonce)
        assertNull(contents.state)
        assertNull(contents.attestations?.idTokens)
        assertNull(contents.attestations?.presentations)
        assertNull(contents.vc)
        assertNull(contents.recipient)
    }

    @Test
    fun `format Issuance Response with presentation attestations`() {
        val key = "test543534"
        val mockedVpFormatterResult = "mockedPresentation"
        val requestedPresentations = mapOf(key to mockedVerifiableCredential)
        `when`<String>(mockedVerifiablePresentationFormatter.createPresentation(listOf(mockedVerifiableCredential), audience, mockedIdentifier, expiresIn)).thenReturn(mockedVpFormatterResult)
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedVcs = requestedPresentations)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertEquals(mapOf(key to mockedVpFormatterResult), contents.attestations?.presentations)
        assertNull(contents.nonce)
        assertNull(contents.state)
        assertNull(contents.attestations?.idTokens)
        assertNull(contents.attestations?.selfIssued)
        assertNull(contents.vc)
        assertNull(contents.recipient)
    }

    @Test
    fun `format Issuance Response with all attestations`() {
        val requestedSelfIssued = mapOf("test3423442" to "self-attested")
        val requestedIdTokens = mapOf("test2332342" to "idToken")
        val key = "test98834"
        val mockedVpFormatterResult = "mockedPresentation"
        val requestedPresentations = mapOf(key to mockedVerifiableCredential)
        `when`<String>(mockedVerifiablePresentationFormatter.createPresentation(listOf(mockedVerifiableCredential), audience, mockedIdentifier, expiresIn)).thenReturn(mockedVpFormatterResult)
        val results = formatter.format(responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedVcs = requestedPresentations,
            requestedSelfIssuedClaims = requestedSelfIssued,
            requestedIdTokens = requestedIdTokens)
        val contents = serializer.parse(OidcResponseContent.serializer(), results)
        assertEquals(audience, contents.aud)
        assertEquals(did, contents.did)
        assertEquals(thumbprint, contents.sub)
        assertEquals(mockedJsonWebKey, contents.subJwk)
        assertEquals(mapOf(key to mockedVpFormatterResult), contents.attestations?.presentations)
        assertEquals(requestedSelfIssued, contents.attestations?.selfIssued)
        assertEquals(requestedIdTokens, contents.attestations?.idTokens)
        assertNull(contents.nonce)
        assertNull(contents.state)
        assertNull(contents.vc)
        assertNull(contents.recipient)
    }
}