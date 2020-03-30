/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer

/**
 * Class that represents a generic Request.
 *
 * @param oidcParameters OpenId Connect specific parameters.
 * @param serializedToken Serialized JwsToken that contains additional request params.
 */
class OidcRequest(val oidcParameters: Map<String, List<String>>, serializedToken: String): Request {

    val content: OidcRequestContent

    val token: JwsToken = JwsToken.deserialize(serializedToken)

    init {
        content = Serializer.parse(OidcRequestContent.serializer(), token.content())
    }

    override fun getCredentialAttestations(): CredentialAttestations {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}