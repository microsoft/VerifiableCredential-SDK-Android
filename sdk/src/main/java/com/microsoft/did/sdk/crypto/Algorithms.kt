// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.spi.EcPairwiseKeySpec
import com.microsoft.did.sdk.util.Constants
import org.spongycastle.jce.ECNamedCurveTable
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.KeySpec

abstract class SigningAlgorithm(val name: String, val provider: String, val spec: AlgorithmParameterSpec? = null) {
    class Secp256k1 : SigningAlgorithm("SHA256WITHPLAIN-ECDSA", "SC")
}

abstract class DigestAlgorithm(val name: String, val provider: String) {
    class Sha256 : DigestAlgorithm("SHA-256", "SC")
    class Rsa : DigestAlgorithm("SHA-512", "SC") // EXAMPLE
}

abstract class CipherAlgorithm(val name: String, val provider: String) {
    class DesCbcPkcs5Padding : DigestAlgorithm("DES/CBC/PKCS5Padding", "SC") // EXAMPLE
}

abstract class KeyAlgorithm(val name: String, val provider: String, val keySpec: KeySpec) {
    class Secp256k1(ecPrivateKeySpec: ECPrivateKeySpec) : KeyAlgorithm("EC", "SC", ecPrivateKeySpec) // EXAMPLE
    class EcPairwise(ecPairwiseKeySpec: EcPairwiseKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwiseKeySpec) // EXAMPLE
}

abstract class KeyGenAlgorithm(val name: String, val provider: String, val spec: AlgorithmParameterSpec) {
    class Secp256k1 : KeyGenAlgorithm("EC", "SC", ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC))
}

abstract class MacAlgorithm(val name: String, val provider: String) {
    class Hmac512 : MacAlgorithm("HMAC512", "SC")
}