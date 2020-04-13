package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.validators.JwsValidator
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OidcRequestValidatorTest {

    @Mock
    lateinit var jwtValidator: JwsValidator

    @Mock
    lateinit var oidcRequestContent: OidcRequestContent

    lateinit var oidcValidator: OidcRequestValidator

    lateinit var request: OidcRequest

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        this.oidcValidator = OidcRequestValidator(jwtValidator)
        this.request = PresentationRequest(mapOf(), "")
    }

    @Test
    fun testValidation() {

//        runBlocking {
//            `when`(oidcRequestContent.clientId).thenReturn("test.com")
//            val isValid = this.oidcValidator.validate(request)
//        }
    }
}

