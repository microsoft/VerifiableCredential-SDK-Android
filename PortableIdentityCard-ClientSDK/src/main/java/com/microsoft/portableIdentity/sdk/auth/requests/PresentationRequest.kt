/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent

class PresentationRequest(oidcParameters: Map<String, List<String>>, serializedToken: String, contents: OidcRequestContent) :
    OidcRequest(oidcParameters, serializedToken, contents)