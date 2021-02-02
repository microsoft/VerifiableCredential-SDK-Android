// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.di

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.util.Constants
import org.spongycastle.jce.ECNamedCurveTable
import java.security.KeyPairGenerator

class DefaultTestKeyStore {

    fun getWithEcKey() {
        val kegGen = KeyPairGenerator.getInstance("EC", "SC")
        kegGen.initialize(ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC))
        kegGen.genKeyPair()
    }
}