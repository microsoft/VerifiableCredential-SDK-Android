// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.spi.java

import com.microsoft.did.sdk.crypto.spi.SignatureSpi
import org.spongycastle.jce.spec.ElGamalParameterSpec
import java.security.*
import java.security.spec.AlgorithmParameterSpec

sealed class JavaSecuritySignature(
    private val algorithm: String,
    private val provider: String,
    private val spec: AlgorithmParameterSpec? = null
) : SignatureSpi() {

    class Secp256k1 : JavaSecuritySignature("SHA256WITHPLAIN-ECDSA", "SC")
//    class ElGamal(params: ElGamalParameterSpec) : JavaSecuritySignature("RSA", "SC", params)

    override fun sign(key: PrivateKey, data: ByteArray): ByteArray {
        val signer = Signature.getInstance(algorithm, provider)
            .apply {
                initSign(key)
                update(data)
            }
        return signer.sign()
    }

    override fun verify(key: PublicKey, signature: ByteArray, data: ByteArray): Boolean {
        val verifier = Signature.getInstance(algorithm, provider)
            .apply {
                initVerify(key)
                update(data)
            }
        return verifier.verify(signature)
    }
}