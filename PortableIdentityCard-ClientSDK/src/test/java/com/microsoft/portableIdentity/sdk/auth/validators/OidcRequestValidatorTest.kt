package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ValidatorException
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.*
import kotlin.test.assertEquals


class OidcRequestValidatorTest {

    @Mock
    lateinit var mockedJwsValidator: JwsValidator

    @Mock
    lateinit var mockedJwsToken: JwsToken

    @Mock
    lateinit var mockedOidcRequest: OidcRequest

    @Mock
    lateinit var mockedGenericRequest: Request

    lateinit var oidcRequestValidator: OidcRequestValidator

    lateinit var mockedOidcRequestContent: OidcRequestContent

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockedJwsToken = JwsToken("token")
        oidcRequestValidator = OidcRequestValidator(mockedJwsValidator)
    }

    private fun setUpOidcRequest(requestClientId: String, parameterClientId: String, mockedExpiration: Long) {
        mockedOidcRequestContent = OidcRequestContent(clientId = requestClientId, exp = mockedExpiration)
        `when`<OidcRequestContent>(mockedOidcRequest.content).thenReturn(mockedOidcRequestContent)
        `when`<Map<String, List<String>>>(mockedOidcRequest.oidcParameters).thenReturn(mapOf("client_id" to listOf(parameterClientId)))
        `when`<JwsToken>(mockedOidcRequest.raw).thenReturn(mockedJwsToken)
        mockedJwsValidator.stub {
            onBlocking { verifySignature(mockedJwsToken) }.doReturn(Result.Success(true))
        }

    }

    @Test
    fun `validate an OIDC Request with all valid properties`() {
        runBlocking {
            val clientId = "testClient123"
            val currentTimePlusTenMinute = (Date().time / Constants.MILLISECONDS_IN_A_SECOND) + 600
            setUpOidcRequest(clientId, clientId, currentTimePlusTenMinute)
            val results = oidcRequestValidator.validate(mockedOidcRequest)
            assertEquals(true, (results as Result.Success<Boolean>).payload)
        }
    }

    @Test
    fun `validate an OIDC Request with expired expiration`() {
        runBlocking {
            val currentTimeMinusTenMinutes = (Date().time / Constants.MILLISECONDS_IN_A_SECOND) - 600
            val clientId = "testClient123"
            setUpOidcRequest(clientId, clientId, currentTimeMinusTenMinutes)
            val results = oidcRequestValidator.validate(mockedOidcRequest)
            assertEquals(false, (results as Result.Success<Boolean>).payload)
        }
    }

    @Test
    fun `validate an OIDC Request with client ids not matching`() {
        runBlocking {
            val currentTimePlusOneMinute = (Date().time / Constants.MILLISECONDS_IN_A_SECOND) + 60
            val requestClientId = "testClient123"
            val parameterClientId = "testClient456"
            setUpOidcRequest(requestClientId, parameterClientId, currentTimePlusOneMinute)
            val results = oidcRequestValidator.validate(mockedOidcRequest)
            assertEquals(false, (results as Result.Success<Boolean>).payload)
        }
    }

    @Test
    fun `validate an OIDC Request with invalid jwsToken`() {
        runBlocking {
            val clientId = "testClient123"
            val currentTimePlusTenMinute = (Date().time / Constants.MILLISECONDS_IN_A_SECOND) + 600
            setUpOidcRequest(clientId, clientId, currentTimePlusTenMinute)
            mockedJwsValidator.stub {
                onBlocking { verifySignature(mockedJwsToken) }.doReturn(Result.Success(false))
            }
            val results = oidcRequestValidator.validate(mockedOidcRequest)
            assertEquals(false, (results as Result.Success<Boolean>).payload)
        }
    }

    @Test
    fun `validate an OIDC Request with wrong Request type`() {
        runBlocking {
            val results = oidcRequestValidator.validate(mockedGenericRequest)
            val expectedException = ValidatorException("Request is not an OidcRequest")
            assertEquals(expectedException.message, (results as Result.Failure).payload.message)
            assert(results.payload is ValidatorException)
        }
    }
}

