// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.linkedDomains

import kotlinx.serialization.Serializable

@Serializable
sealed class LinkedDomainResult(val domain: String)

@Serializable
class LinkedDomainVerified(val domainUrl: String) : LinkedDomainResult(domainUrl)

@Serializable
class LinkedDomainUnVerified(val domainUrl: String) : LinkedDomainResult(domainUrl)