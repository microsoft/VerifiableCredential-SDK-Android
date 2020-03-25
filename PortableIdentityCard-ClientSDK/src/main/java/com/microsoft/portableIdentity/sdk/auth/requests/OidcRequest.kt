/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import io.ktor.http.Url
import io.ktor.util.toMap

/**
 * Class that represents a generic Request.
 *
 * @param rawRequest to be parsed.
 * @param validator optional parameter used to validate request.
 */
class OidcRequest(private val rawRequest: String): Request {

    val requestParameters: Map<String, List<String>>

    private var requestToken: JwsToken? = null

    private var requestContent: OidcRequestContent? = null

    private var requestUri: String? = null

    init {
        val openIdUrl = Url(rawRequest)
        requestParameters = openIdUrl.parameters.toMap()

        val serializedToken = requestParameters["request"]?.first()

        if (serializedToken != null) {
            requestToken = JwsToken.deserialize(serializedToken, BaseLogger)
            requestContent = Serializer.parse(OidcRequestContent.serializer(), requestToken!!.content())
        } else {
            requestUri = requestParameters["request_uri"]?.first()
        }
    }

    override fun getCredentialRequests(): CredentialRequests {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getRequestUri(): String? {
        return requestUri
    }

    fun getJwsToken(): JwsToken? {
        return requestToken
    }

    fun getContents(): OidcRequestContent? {
        return requestContent
    }
}