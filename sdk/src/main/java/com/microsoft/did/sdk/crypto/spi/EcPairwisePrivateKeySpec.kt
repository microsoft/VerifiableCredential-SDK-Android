// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import java.security.spec.KeySpec

class EcPairwisePrivateKeySpec(
    val personaSeed: ByteArray,
    val userDid: String,
    val peerDid: String
) : KeySpec