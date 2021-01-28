// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.util.controlflow.UnSupportedOperationException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

abstract class KeyPairGeneratorSpi {
    open fun generateKeyPair(): KeyPair {
        throw UnSupportedOperationException("GenerateKeyPair not supported.")
    }
}