// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.linkedDomains

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class LinkedDomainResult

@Serializable
@SerialName("LinkedDomainVerified")
class LinkedDomainVerified(val domainUrl: String) : LinkedDomainResult()

@Serializable
@SerialName("LinkedDomainUnVerified")
class LinkedDomainUnVerified(val domainUrl: String) : LinkedDomainResult()

@Serializable
@SerialName("LinkedDomainMissing")
object LinkedDomainMissing : LinkedDomainResult()