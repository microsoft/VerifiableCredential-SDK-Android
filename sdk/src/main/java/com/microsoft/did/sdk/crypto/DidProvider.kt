// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.spi.EcPairwiseKeyFactorySpi
import java.security.Provider

class DidProvider : Provider("DID", 1.0, "Custom cryptographic operations for Decentralized Identity") {

    init {
        put("KeyFactory.EcPairwise", EcPairwiseKeyFactorySpi::class.qualifiedName)
    }
}