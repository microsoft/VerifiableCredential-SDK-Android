// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import kotlinx.serialization.json.Json

// Keep in sync with `fun defaultJsonSerializer()` in SdkModule
val defaultTestSerializer = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
    isLenient = true
}