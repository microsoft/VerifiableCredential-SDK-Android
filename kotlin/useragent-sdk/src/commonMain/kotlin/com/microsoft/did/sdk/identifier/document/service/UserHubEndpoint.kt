package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.Serializable

@Serializable
class UserHubEndpoint(val instances: List<String>): Endpoint()