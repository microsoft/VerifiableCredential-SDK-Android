// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.util.Constants.SECP256K1_CURVE_NAME_EC
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.*

class Secp256k1Provider : Provider() {
    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
    }

    companion object {
        private const val KEY_ALGORITHM = "EC"
        private const val KEY_PROVIDER = "SC"
        private const val SIGNATURE_ALGORITHM = "SHA256WITHPLAIN-ECDSA"
        private const val PROVIDER = "SC"
    }

    override fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, KEY_PROVIDER)
        keyGen.initialize(ECNamedCurveTable.getParameterSpec(SECP256K1_CURVE_NAME_EC))
        return keyGen.genKeyPair()
    }

    override fun sign(key: PrivateKey, data: ByteArray): ByteArray {
        val signer = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER)
                .apply {
                    initSign(key)
                    update(data)
                }
        return signer.sign()
    }

    override fun verify(key: PublicKey, signature: ByteArray, data: ByteArray): Boolean {
        val verifier = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER)
            .apply {
                initVerify(key)
                update(data)
            }
        return verifier.verify(signature)
    }
}