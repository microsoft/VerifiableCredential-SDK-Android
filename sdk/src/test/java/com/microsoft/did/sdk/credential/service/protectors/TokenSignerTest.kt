package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class TokenSignerTest {

    // mocks for retrieving kid.
    @Mock
    lateinit var mockedCryptoOperations: CryptoOperations
    @Mock
    lateinit var mockedKeyStore: KeyStore
    @Mock
    lateinit var mockedKeyContainer: KeyContainer<PrivateKey>
    @Mock
    lateinit var mockedPrivateKey: PrivateKey

    @Mock
    lateinit var mockedIdentifier: Identifier

    @Mock
    lateinit var mockedJwsToken: JwsToken

    lateinit var signer: TokenSigner

    // TODO random numbers for these strings.
    private val signingKeyRef = "sigKeyRef5237"
    private val did = "did:test:567812"
    private val kid = "#kid1426"
    private val alg = "ES256K"
    private val payload = "payload12423"
    private val expectedHeader = mapOf("kid" to "$did$kid")
    private val serializer = Serializer()
    private val expectedSignedPayload = "signedPayload45236"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        signer = TokenSigner(mockedCryptoOperations, serializer)
        setUpGetPrivateKey()
        setUpIdentifier()
    }

    private fun setUpGetPrivateKey() {
        `when`<KeyStore>(mockedCryptoOperations.keyStore).thenReturn(mockedKeyStore)
        `when`<KeyContainer<PrivateKey>>(mockedKeyStore.getPrivateKey(signingKeyRef)).thenReturn(mockedKeyContainer)
        `when`<PublicKey>(mockedKeyContainer.getKey()).thenReturn(mockedPrivateKey)
        `when`<String>(mockedPrivateKey.kid).thenReturn(kid)
        `when`<String>(mockedPrivateKey.alg).thenReturn(alg)
    }
    //
    private fun setUpIdentifier() {
        `when`<String>(mockedIdentifier.signatureKeyReference).thenReturn(signingKeyRef)
        `when`<String>(mockedIdentifier.id).thenReturn(did)
    }
}