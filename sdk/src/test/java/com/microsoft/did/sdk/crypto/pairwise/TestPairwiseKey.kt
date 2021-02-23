// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.pairwise

import kotlinx.serialization.Serializable

@Serializable
data class TestPairwiseKey(val pwid: String, val key: String)

@Serializable
data class TestKeys(val keys: List<TestPairwiseKey>)