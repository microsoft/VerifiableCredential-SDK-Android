// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi

import com.microsoft.did.sdk.util.controlflow.UnSupportedOperationException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

abstract class SignatureSpi {
//    open fun digest(data: ByteArray): ByteArray {
//        throw UnSupportedOperationException("Digest not supported.")
//    }
//


    open fun sign(key: PrivateKey, data: ByteArray): ByteArray {
        throw UnSupportedOperationException("Sign not supported.")
    }

    open fun verify(key: PublicKey, signature: ByteArray, data: ByteArray): Boolean {
        throw UnSupportedOperationException("Verify not supported.")
    }

//    open fun encrypt(key: SecretKey, data: ByteArray): ByteArray {
//        throw UnSupportedOperationException("Encrypt not supported.")
//    }
//
//    open fun decrypt(key: SecretKey, data: ByteArray): ByteArray {
//        throw UnSupportedOperationException("Decrypt not supported.")
//    }
}