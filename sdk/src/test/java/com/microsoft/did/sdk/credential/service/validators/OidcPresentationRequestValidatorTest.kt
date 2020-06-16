package com.microsoft.did.sdk.credential.service.validators

import android.net.Uri
import com.microsoft.did.sdk.credential.service.models.oidc.OidcRequestContent
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import org.junit.Before
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OidcPresentationRequestValidatorTest {

    @Mock
    lateinit var mockedPresentationRequest: PresentationRequest
    @Mock
    lateinit var mockedJwsToken: JwsToken
    @Mock
    lateinit var mockedJwsValidator: JwsValidator
    @Mock
    lateinit var mockedCryptoOperations: CryptoOperations
    @Mock
    lateinit var mockedPublicKey: PublicKey
    @Mock
    lateinit var mockedUri: Uri

    lateinit var validator: OidcPresentationRequestValidator

    private val signingKeyRef: String = "sigKeyRef1243523"
    private val did: String = "did:test:2354543"
    private val audience: String = "audience2432"
    private val mockedSerializedToken: String = "token2364302"
    private val clientId: String = "clientId12453"
    private val serializer: Serializer =
        Serializer()
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
        validator = OidcPresentationRequestValidator(mockedJwsValidator, serializer)
        setUpPresentationRequest()
        `when`<String>(mockedPresentationRequest.serializedToken).thenReturn(mockedSerializedToken)
        `when`<Boolean>(mockedJwsToken.verify(mockedCryptoOperations, listOf(mockedPublicKey))).thenReturn(true)
    }

    private fun setUpPresentationRequest() {
        `when`<String>(mockedPresentationRequest.serializedToken).thenReturn(mockedSerializedToken)
        `when`<OidcRequestContent>(mockedPresentationRequest.content).thenReturn(OidcRequestContent(clientId = clientId))
        `when`<Uri>(mockedPresentationRequest.uri).thenReturn(mockedUri)
        `when`<String>(mockedUri.getQueryParameter(ArgumentMatchers.anyString())).thenReturn(clientId)
    }

//    @Test
//    fun `validate a presentation request`() {
//        TODO("not implemented")
//    }
}