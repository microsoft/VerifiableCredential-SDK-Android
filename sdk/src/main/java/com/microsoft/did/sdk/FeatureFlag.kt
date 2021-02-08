// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlag @Inject constructor() {
    var linkedDomains: Boolean = false
}