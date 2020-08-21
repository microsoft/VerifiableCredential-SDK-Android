// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.Serializable

@Serializable
abstract class Parent {
    var name: String = ""
    var std: String = ""
}