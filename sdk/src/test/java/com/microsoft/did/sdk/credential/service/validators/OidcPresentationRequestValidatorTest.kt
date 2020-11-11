/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.ExpiredTokenExpirationException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Date
import kotlin.test.fail

class OidcPresentationRequestValidatorTest {

    private val mockedPresentationRequest: PresentationRequest = mockk()

    private val mockedJwsToken: JwsToken = mockk()

    private val mockedJwtValidator: JwtValidator = mockk()

    private val mockedOidcRequestContent: PresentationRequestContent = mockk()

    private val mockedIdentifier: Identifier = mockk()

    private val expectedSerializedToken: String = "token2364302"

    private val validator: OidcPresentationRequestValidator = OidcPresentationRequestValidator()
    
	private val serializer: Json = Json

    private val expectedSigningKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"
    private val expectedResponseType = "id_token"
    private val expectedResponseMode = "form_post"
    private val expectedScope = "openid did_authn"
    private val expectedExpirationTime = 86400L

    init {
        setUpPresentationRequest()
        setUpIdentifier()
        mockkObject(JwsToken)
    }

    private fun setUpPresentationRequest() {
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
    }

    private fun setUpIdentifier() {
        every { mockedIdentifier.signatureKeyReference } returns expectedSigningKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    private fun setUpExpiration(offsetInSecond: Long) {
        val currentTimeInSeconds: Long = Date().time / Constants.MILLISECONDS_IN_A_SECOND
        val currentTimePlusOffsetInSeconds = currentTimeInSeconds + offsetInSecond
        every { mockedOidcRequestContent.expirationTime } returns currentTimePlusOffsetInSeconds
    }

    private fun setUpOidcRequestContent() {
        every { mockedOidcRequestContent.responseType } returns expectedResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedResponseMode
        every { mockedOidcRequestContent.scope } returns expectedScope
    }

    @Test
    fun `valid signature is validated successfully`() {
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns listOf(mockk())
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContent()
        every { JwsToken.deserialize(expectedSerializedToken, serializer) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns true
        runBlocking {
            validator.validate(mockedPresentationRequest)
        }
    }

    @Test
    fun `throws when token expiration is expired`() {
        setUpExpiration(-86400)
        setUpOidcRequestContent()
        every { JwsToken.deserialize(expectedSerializedToken, serializer) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns true
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
                fail()
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ExpiredTokenExpirationException::class.java)
            }
        }
    }
}
