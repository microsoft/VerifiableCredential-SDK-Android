// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.requests

class PresentationRequest(override val oidcParameters: Map<String, List<String>>, serializedToken: String): OidcRequest(oidcParameters, serializedToken) {
}