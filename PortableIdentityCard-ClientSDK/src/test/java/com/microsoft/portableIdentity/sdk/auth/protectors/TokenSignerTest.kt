package com.microsoft.portableIdentity.sdk.auth.protectors

import android.media.session.MediaSession
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyContainer
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class TokenSignerTest {

    // MOCKS FOR RETRIEVING KID
    @Mock
    lateinit var mockedCryptoOperations: CryptoOperations
    @Mock
    lateinit var mockedKeyStore: KeyStore
    @Mock
    lateinit var mockedKeyContainer: KeyContainer<PublicKey>
    @Mock
    lateinit var mockedPublicKey: PublicKey

    @Mock
    lateinit var mockedIdentifier: Identifier

    @Mock
    lateinit var mockedSerializer : Serializer

    lateinit var signer: TokenSigner

    // TODO random numbers for these strings.
    private val signingKeyRef = "sigKeyRef5237"
    private val did = "did:test:567812"
    private val kid = "kid1426"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        signer = TokenSigner(mockedCryptoOperations, mockedSerializer)
        setUpGetPublicKey(signingKeyRef)
        setUpIdentifier(signingKeyRef)
    }

    private fun setUpGetPublicKey(signingKeyRef: String) {
        Mockito.`when`<KeyStore>(mockedCryptoOperations.keyStore).thenReturn(mockedKeyStore)
        Mockito.`when`<KeyContainer<PublicKey>>(mockedKeyStore.getPublicKey(signingKeyRef)).thenReturn(mockedKeyContainer)
        Mockito.`when`<PublicKey>(mockedKeyContainer.getKey()).thenReturn(mockedPublicKey)
        Mockito.`when`<String>(mockedPublicKey.kid).thenReturn(kid)
    }
    //
    private fun setUpIdentifier(signingKeyRef: String) {
        Mockito.`when`<String>(mockedIdentifier.signatureKeyReference).thenReturn(signingKeyRef)
    }

    @Test
    fun `Sign Payload`() {
        Mockito.`when`<String>(mockedIdentifier.signatureKeyReference).thenReturn(signingKeyRef)
        val testSignedPayload = signer.signWithIdentifier("payload", mockedIdentifier)
    }
}