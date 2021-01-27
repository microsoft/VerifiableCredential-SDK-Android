// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.provider

import com.microsoft.did.sdk.util.controlflow.UnSupportedOperationException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

abstract class Provider {
    open fun digest(data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Digest not supported.")
    }

    open fun generateKey(): SecretKey {
        throw UnSupportedOperationException("GenerateKey not supported.")
    }

    open fun generateKeyPair(): KeyPair {
        throw UnSupportedOperationException("GenerateKeyPair not supported.")
    }

    open fun sign(key: PrivateKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Sign not supported.")
    }

    open fun verify(key: PublicKey, signature: ByteArray, data: ByteArray): Boolean {
        throw UnSupportedOperationException("Verify not supported.")
    }

    open fun encrypt(key: SecretKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Encrypt not supported.")
    }

    protected open fun decrypt(key: SecretKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Decrypt not supported.")
    }
}