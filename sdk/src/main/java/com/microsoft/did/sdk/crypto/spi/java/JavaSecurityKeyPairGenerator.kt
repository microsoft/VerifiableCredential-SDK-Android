// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi.java

import com.microsoft.did.sdk.crypto.spi.KeyPairGeneratorSpi
import com.microsoft.did.sdk.util.Constants
import org.spongycastle.jce.ECNamedCurveTable
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.AlgorithmParameterSpec

sealed class JavaSecurityKeyPairGenerator(
    private val algorithm: String,
    private val provider: String,
    private val spec: AlgorithmParameterSpec
) : KeyPairGeneratorSpi() {

    class Secp256k1 : JavaSecurityKeyPairGenerator("EC", "SC", ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC))

    override fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(algorithm, provider)
        keyGen.initialize(spec)
        return keyGen.genKeyPair()
    }
}