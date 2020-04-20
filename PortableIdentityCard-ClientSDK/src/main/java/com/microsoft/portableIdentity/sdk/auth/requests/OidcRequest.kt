/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.json.JsonNull.content

/**
 * Class that represents a generic Request.
 *
 * @param oidcParameters OpenId Connect specific parameters.
 * @param serializedToken Serialized JwsToken that contains additional request params.
 */
abstract class OidcRequest(val oidcParameters: Map<String, List<String>>, val rawToken: String, val content: OidcRequestContent): CredentialRequest(content.attestations)