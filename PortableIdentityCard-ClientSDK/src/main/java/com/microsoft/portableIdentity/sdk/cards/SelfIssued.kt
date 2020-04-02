// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.cards

import kotlinx.serialization.Serializable

@Serializable
data class SelfIssued(
        val selfIssued: Map<String, String>
)