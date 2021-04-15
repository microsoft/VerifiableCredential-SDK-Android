// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.internal

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlag @Inject constructor() {
    var linkedDomains: Boolean = true
}