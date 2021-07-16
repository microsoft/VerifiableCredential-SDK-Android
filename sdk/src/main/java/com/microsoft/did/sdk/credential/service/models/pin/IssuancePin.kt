// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.pin

import kotlinx.serialization.Serializable

@Serializable
data class IssuancePin(var pin: String) {
    var pinSalt: String? = null
    var pinAlg: String? = null
    var iterations: Int = 0
}