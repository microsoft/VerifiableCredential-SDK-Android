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
import com.microsoft.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*
import kotlin.test.fail

class OidcPresentationRequestValidatorTest {

    private val mockedPresentationRequest: PresentationRequest = mockk()

    private val mockedJwsToken: JwsToken = mockk()

    private val mockedJwtValidator: JwtValidator = mockk()

    private val mockedOidcRequestContentRequestContent: PresentationRequestContent = mockk()

    private val mockedIdentifier: Identifier = mockk()

    private val expectedSerializedToken: String = "token2364302"

    private val validator: OidcPresentationRequestValidator
    private val serializer: Serializer = Serializer()

    private val expectedSigningKeyRef: String = "sigKeyRef1243523"
    private val expectedDid: String = "did:test:2354543"

    init {
        validator = OidcPresentationRequestValidator(mockedJwtValidator, serializer)
        setUpPresentationRequest()
        setUpIdentifier()
        mockkObject(JwsToken)
    }

    private fun setUpPresentationRequest() {
        every { mockedPresentationRequest.serializedToken } returns expectedSerializedToken
        every { mockedPresentationRequest.requestContent } returns mockedOidcRequestContentRequestContent
    }

    private fun setUpIdentifier() {
        every { mockedIdentifier.signatureKeyReference } returns expectedSigningKeyRef
        every { mockedIdentifier.id } returns expectedDid
    }

    private fun setUpExpiration(offsetInSecond: Long) {
        val currentTimeInSeconds: Long = Date().time / Constants.MILLISECONDS_IN_A_SECOND
        val currentTimePlusOffsetInSeconds = currentTimeInSeconds + offsetInSecond
        every { mockedOidcRequestContentRequestContent.expirationTime } returns currentTimePlusOffsetInSeconds
    }

    @Test
    fun `valid signature is validated successfully`() {
        setUpExpiration(86400)
        every { JwsToken.deserialize(expectedSerializedToken, serializer) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns true
        runBlocking {
                validator.validate(mockedPresentationRequest)
        }
    }

    @Test
    fun `invalid signature fails successfully`() {
        setUpExpiration(86400)
        every { JwsToken.deserialize(expectedSerializedToken, serializer) } returns mockedJwsToken
        coEvery { mockedJwtValidator.verifySignature(mockedJwsToken) } returns false
        runBlocking {
            try {
                validator.validate(mockedPresentationRequest)
                fail()
            } catch (exception: Exception) {
                assertThat(exception).isInstanceOf(InvalidSignatureException::class.java)
            }
        }
    }

    @Test
    fun `throws when token expiration is expired`() {
        setUpExpiration(-86400)
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
