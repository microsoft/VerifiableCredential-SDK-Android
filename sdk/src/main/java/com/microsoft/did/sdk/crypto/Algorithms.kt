// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.spi.EcPairwisePrivateKeySpec
import com.microsoft.did.sdk.crypto.spi.EcPairwisePublicKeySpec
import com.nimbusds.jose.jwk.Curve
import java.math.BigInteger
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPrivateKeySpec
import java.security.spec.ECPublicKeySpec
import java.security.spec.KeySpec

abstract class SigningAlgorithm(val name: String, val provider: String?, val spec: AlgorithmParameterSpec? = null) {
    class Secp256k1 : SigningAlgorithm("SHA256withPLAIN-ECDSA", "BC") // EXAMPLE
}

abstract class DigestAlgorithm(val name: String, val provider: String?) {
    class Sha256 : DigestAlgorithm("SHA-256", null)
}

abstract class CipherAlgorithm(val name: String, val provider: String?)

abstract class PrivateKeyFactoryAlgorithm(val name: String, val provider: String?, val keySpec: KeySpec) {
    class EcPairwise(ecPairwisePrivateKeySpec: EcPairwisePrivateKeySpec) :
        PrivateKeyFactoryAlgorithm("ecPairwise", "DID", ecPairwisePrivateKeySpec)

    class Secp256k1(s: BigInteger) :
        PrivateKeyFactoryAlgorithm("EC", null, ECPrivateKeySpec(s, Curve.SECP256K1.toECParameterSpec()))
}

abstract class PublicKeyFactoryAlgorithm(val name: String, val provider: String?, val keySpec: KeySpec) {
    class Secp256k1(x: BigInteger, y: BigInteger) :
        PublicKeyFactoryAlgorithm("EC", null, ECPublicKeySpec(ECPoint(x, y), Curve.SECP256K1.toECParameterSpec()))

    class EcPairwise(ecPairwisePublicKeySpec: EcPairwisePublicKeySpec) :
        PublicKeyFactoryAlgorithm("ecPairwise", "DID", ecPairwisePublicKeySpec)
}

abstract class KeyGenAlgorithm(val name: String, val provider: String?, val spec: AlgorithmParameterSpec) {
    class Secp256k1 : KeyGenAlgorithm("EC", null, Curve.SECP256K1.toECParameterSpec())
}

abstract class MacAlgorithm(val name: String, val provider: String?) {
    class HmacSha512 : MacAlgorithm("HmacSHA512", null)
}