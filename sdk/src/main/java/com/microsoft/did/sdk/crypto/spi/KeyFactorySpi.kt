// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.util.controlflow.UnSupportedOperationException
import java.security.PrivateKey
import java.security.PublicKey

abstract class KeyFactorySpi {
    fun generatePrivateKey(): PrivateKey {
        throw UnSupportedOperationException("Not supported.")
    }

    fun generatePublicKey(): PublicKey {
        throw UnSupportedOperationException("Not supported.")
    }
}