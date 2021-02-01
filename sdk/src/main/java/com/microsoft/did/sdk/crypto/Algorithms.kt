// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.spi.EcPairwisePrivateKeySpec
import com.microsoft.did.sdk.crypto.spi.EcPairwisePublicKeySpec
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.Curve
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.interfaces.ECPublicKey
import java.security.spec.ECPoint
import java.math.BigInteger
import java.security.Provider
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPublicKeySpec
import java.security.spec.KeySpec
import java.security.spec.RSAPublicKeySpec

abstract class SigningAlgorithm(val name: String, val provider: String?, val spec: AlgorithmParameterSpec? = null) {
    class Secp256k1 : SigningAlgorithm("SHA256WITHPLAIN-ECDSA", "SC")
}

abstract class DigestAlgorithm(val name: String, val provider: String?) {
    class Sha256 : DigestAlgorithm("SHA-256", "SC")
}

abstract class CipherAlgorithm(val name: String, val provider: String?) {
    class DesCbcPkcs5Padding : DigestAlgorithm("DES/CBC/PKCS5Padding", "SC") // EXAMPLE
}

abstract class KeyAlgorithm(val name: String, val provider: String?, val keySpec: KeySpec) {
    class EcPrivatePairwise(ecPairwisePrivateKeySpec: EcPairwisePrivateKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwisePrivateKeySpec)
    class EcPublicPairwise(ecPairwisePublicKeySpec: EcPairwisePublicKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwisePublicKeySpec)
    class RSAPublic(keySpec: RSAPublicKeySpec) : KeyAlgorithm("RSA", "SC", keySpec)
    class Secp256k1Public(x: BigInteger, y: BigInteger): KeyAlgorithm("EC", null, ECPublicKeySpec(ECPoint(x, y), Curve.SECP256K1.toECParameterSpec()))
}

abstract class KeyGenAlgorithm(val name: String, val provider: String?, val spec: AlgorithmParameterSpec) {
    class Secp256k1 : KeyGenAlgorithm("EC", "SC", ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC))
}

abstract class MacAlgorithm(val name: String, val provider: String?) {
    class Hmac512 : MacAlgorithm("HMAC512", "SC")
}