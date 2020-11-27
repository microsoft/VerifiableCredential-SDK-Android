/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.CREDENTIAL_PRESENTATION_FORMAT
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that can protect some content by signing.
 */
@Singleton
class TokenSigner @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Json
) {
    private val testName = "TokenSigner"
    private val kidCache = HashMap<String, String>()

    /**
     * Sign content with keyReference.
     * @return JwsToken
     */
    fun signWithIdentifier(payload: String, identifier: Identifier): String {
        println("PerfTest->(${getTestName()}) in  μs - 0: Start TokenSigner setup: 0")
        var startTime = getStartTime()

        val token = JwsToken(payload, serializer)
        println("PerfTest->(${getTestName()}) in  μs - 0: End  TokenSigner setup: ${timer(startTime)}")

        println("PerfTest->(${getTestName()}) in  μs - 0: Start key get: 0")
        startTime = getStartTime()

        var kid = kidCache.get(identifier.id)
        if (kid == null) {
            // TODO make more bullet proof in case key changes
            kid = cryptoOperations.keyStore.getPrivateKey(identifier.signatureKeyReference).getKey().kid
            kidCache.put(identifier.id, kid)
        }

        // adding kid value to header.
        val additionalHeaders = mutableMapOf<String, String>()
        additionalHeaders[JoseConstants.Kid.value] = "${identifier.id}${kid}"
        additionalHeaders[JoseConstants.Type.value] = CREDENTIAL_PRESENTATION_FORMAT
        println("PerfTest->(${getTestName()}) in  μs - 0: End key get: ${timer(startTime)}")

        println("PerfTest->(${getTestName()}) in  μs - 0: Start TokenSigner sign: 0")
        startTime = getStartTime()
        token.sign(identifier.signatureKeyReference, cryptoOperations, additionalHeaders)
        println("PerfTest->(${getTestName()}) in  μs - 0: End  TokenSigner sign: ${timer(startTime)}")
        println("PerfTest->(${getTestName()}) in  μs - 0: Start TokenSigner serialize: 0")
        startTime = getStartTime()
        val serialized = token.serialize(serializer)
        println("PerfTest->(${getTestName()}) in  μs - 0: End  TokenSigner serialize: ${timer(startTime)}")
        return serialized
    }

    fun getTestName(): String {
        return this.testName
    }

    fun getStartTime(): Long {
        return System.nanoTime()
    }

    fun timer(start: Long): String {
        val timing = System.nanoTime() - start
        return (timing / 1000).toString()
    }

}