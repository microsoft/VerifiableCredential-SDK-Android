// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.spi.EcPairwiseKeySpec
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.KeySpec

open class SigningAlgorithm(val name: String, val provider: String, val spec: AlgorithmParameterSpec? = null) {
    class Secp256k1 : SigningAlgorithm("SHA256WITHPLAIN-ECDSA", "SC")
}

open class DigestAlgorithm(val name: String, val provider: String) {
    class Sha256 : DigestAlgorithm("SHA-256", "SC")
    class Rsa : DigestAlgorithm("SHA-512", "SC") // EXAMPLE
}

open class CipherAlgorithm(val name: String, val provider: String) {
    class DesCbcPkcs5Padding : DigestAlgorithm("DES/CBC/PKCS5Padding", "SC") // EXAMPLE
}

open class KeyAlgorithm(val name: String, val provider: String, val keySpec: KeySpec) {
    class Secp256(ecPrivateKeySpec: ECPrivateKeySpec) : KeyAlgorithm("EC", "SC", ecPrivateKeySpec) // EXAMPLE
    class EcPairwise(ecPairwiseKeySpec: EcPairwiseKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwiseKeySpec) // EXAMPLE
}

open class MacAlgorithm(val name: String, val provider: String) {
    class Hmac512 : MacAlgorithm("HMAC512", "SC")
}