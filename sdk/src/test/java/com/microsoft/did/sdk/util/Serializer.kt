// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.di.SdkModule

// Keep in sync with `fun defaultJsonSerializer()` in SdkModule
val defaultTestSerializer = SdkModule().defaultJsonSerializer()