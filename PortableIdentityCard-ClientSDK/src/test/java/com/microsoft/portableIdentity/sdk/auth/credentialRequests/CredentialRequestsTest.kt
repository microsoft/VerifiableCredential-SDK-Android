package com.microsoft.portableIdentity.sdk.auth.credentialRequests

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlin.random.Random
import kotlin.test.Test

class CredentialRequestsTest {

    @Test
    fun testSerializer() {

        val inputClaim = InputClaim(true, "test")
        val idTokenRequests = mapOf("test" to inputClaim) as CredentialRequests
        //val serializedIdTokenRequest = Serializer.stringify(CredentialRequests.serializer(), idTokenRequests)
        print(idTokenRequests)
    }
}