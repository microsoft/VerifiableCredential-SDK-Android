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
import com.microsoft.did.sdk.util.controlflow.ExpiredTokenException
import com.microsoft.did.sdk.util.controlflow.InvalidResponseModeException
import com.microsoft.did.sdk.util.controlflow.InvalidResponseTypeException
import com.microsoft.did.sdk.util.controlflow.InvalidScopeException
import com.microsoft.did.sdk.util.controlflow.MissingInputInRequestException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Date

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
    private val expectedValidResponseType = "id_token"
    private val expectedValidResponseMode = "form_post"
    private val expectedValidScope = "openid did_authn"
    private val expectedExpirationTime = 86400L

    private val expectedInvalidResponseMode = "invalid_response_mode"
    private val expectedInvalidResponseType = "invalid_response_type"
    private val expectedInvalidScope = "invalid_scope"

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

    private fun setUpOidcRequestContentWithValidFields() {
        every { mockedOidcRequestContent.responseType } returns expectedValidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedValidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedValidScope
    }

    private fun setUpOidcRequestContentWithInvalidResponseMode() {
        every { mockedOidcRequestContent.responseType } returns expectedValidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedInvalidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedValidScope
    }

    private fun setUpOidcRequestContentWithInvalidResponseType() {
        every { mockedOidcRequestContent.responseType } returns expectedInvalidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedValidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedValidScope
    }

    private fun setUpOidcRequestContentWithInvalidScope() {
        every { mockedOidcRequestContent.responseType } returns expectedValidResponseType
        every { mockedOidcRequestContent.responseMode } returns expectedValidResponseMode
        every { mockedOidcRequestContent.scope } returns expectedInvalidScope
    }

    @Test
    fun `valid signature is validated successfully`() {
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns listOf(mockk())
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithValidFields()
        every { JwsToken.deserialize(expectedSerializedToken) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns true
        runBlocking {
            validator.validate(mockedPresentationRequest)
        }
    }

    @Test
    fun `throws when token expiration is expired`() {
        setUpExpiration(-86400)
        setUpOidcRequestContentWithValidFields()
        every { JwsToken.deserialize(expectedSerializedToken) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns true
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(ExpiredTokenException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has invalid response mode`() {
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns listOf(mockk())
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithInvalidResponseMode()
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(InvalidResponseModeException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has invalid response type`() {
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns listOf(mockk())
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithInvalidResponseType()
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(InvalidResponseTypeException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has invalid scope`() {
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns listOf(mockk())
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithInvalidScope()
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(InvalidScopeException::class.java)
            }
        }
    }

    @Test
    fun `throws when request has missing input`() {
        setUpExpiration(86400)
        every { mockedPresentationRequest.getPresentationDefinition().credentialPresentationInputDescriptors } returns emptyList()
        every { mockedPresentationRequest.content } returns mockedOidcRequestContent
        setUpOidcRequestContentWithValidFields()
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(MissingInputInRequestException::class.java)
            }
        }
    }
}
