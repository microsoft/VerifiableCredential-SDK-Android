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
import com.microsoft.did.sdk.util.serializer.Serializer
import com.nhaarman.mockitokotlin2.eq
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OidcResponseFormatterTest {

    // mocks for retrieving public key.
    @Mock
    lateinit var mockedCryptoOperations: CryptoOperations
    @Mock
    lateinit var mockedKeyStore: KeyStore
    @Mock
    lateinit var mockedKeyContainer: KeyContainer<PublicKey>
    @Mock
    lateinit var mockedPublicKey: PublicKey

    @Mock
    lateinit var mockedTokenSigner: TokenSigner
    @Mock
    lateinit var mockedVerifiablePresentationFormatter: VerifiablePresentationFormatter
    @Mock
    lateinit var mockedVerifiableCredential: VerifiableCredential

    lateinit var formatter: OidcResponseFormatter

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val did: String = "did:test:2354543"
    private val audience: String = "audience2432"
    private val thumbprint: String = "thumbprint534233"
    private val expiresIn: Int = 42
    private val serializer: Serializer =
        Serializer()
    private val mockedJsonWebKey = JsonWebKey()
    private val mockedIdentifier = Identifier(
        did,
        "",
        signingKeyRef,
        "",
        "",
        "",
        ""
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        formatter = OidcResponseFormatter(
            mockedCryptoOperations,
            serializer,
            mockedVerifiablePresentationFormatter,
            mockedTokenSigner
        )
        setUpGetPublicKey()
        `when`<String>(mockedTokenSigner.signWithIdentifier(ArgumentMatchers.anyString(), eq(mockedIdentifier)))
            .thenAnswer { it.arguments[0] }
    }

    private fun setUpGetPublicKey() {
        `when`<KeyStore>(mockedCryptoOperations.keyStore).thenReturn(mockedKeyStore)
        `when`<KeyContainer<PublicKey>>(mockedKeyStore.getPublicKey(signingKeyRef)).thenReturn(mockedKeyContainer)
        `when`<PublicKey>(mockedKeyContainer.getKey()).thenReturn(mockedPublicKey)
        `when`<String>(mockedPublicKey.getThumbprint(mockedCryptoOperations, Sha.SHA256.algorithm)).thenReturn(thumbprint)
        `when`<JsonWebKey>(mockedPublicKey.toJWK()).thenReturn(mockedJsonWebKey)
    }

    @Test
    fun `format simple SIOP response`() {
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn
        )
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
    fun `format presentation response with no attestations`() {
        val nonce = "123456789876"
        val state = "mockedState"
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            nonce = nonce,
            state = state
        )
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
    fun `format issuance response with no attestations`() {
        val contractUrl = "http://testcontract.com"
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            contract = contractUrl
        )
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
    fun `format issuance response with id-token attestations`() {
        val requestedIdTokens = mapOf("test235445" to "idToken")
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedIdTokens = requestedIdTokens
        )
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
    fun `format issuance response with self-attested attestations`() {
        val requestedSelfIssued = mapOf("test3342" to "self-attested")
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedSelfIssuedClaims = requestedSelfIssued
        )
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
        `when`<String>(
            mockedVerifiablePresentationFormatter.createPresentation(
                listOf(mockedVerifiableCredential),
                audience,
                mockedIdentifier,
                expiresIn
            )
        ).thenReturn(mockedVpFormatterResult)
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedVcs = requestedPresentations
        )
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
        `when`<String>(
            mockedVerifiablePresentationFormatter.createPresentation(
                listOf(mockedVerifiableCredential),
                audience,
                mockedIdentifier,
                expiresIn
            )
        ).thenReturn(mockedVpFormatterResult)
        val results = formatter.format(
            responder = mockedIdentifier,
            audience = audience,
            expiresIn = expiresIn,
            requestedVcs = requestedPresentations,
            requestedSelfIssuedClaims = requestedSelfIssued,
            requestedIdTokens = requestedIdTokens
        )
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