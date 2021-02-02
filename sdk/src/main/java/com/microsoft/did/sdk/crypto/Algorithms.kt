// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.spi.EcPairwisePrivateKeySpec
import com.microsoft.did.sdk.crypto.spi.EcPairwisePublicKeySpec
import com.microsoft.did.sdk.util.Constants
import com.nimbusds.jose.jwk.Curve
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve
import java.security.spec.ECPoint
import java.math.BigInteger
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve
import java.security.spec.KeySpec
import java.security.spec.RSAPublicKeySpec

abstract class SigningAlgorithm(val name: String, val provider: String?, val spec: AlgorithmParameterSpec? = null) {
    class Secp256k1 : SigningAlgorithm("SHA256WITHPLAIN-ECDSA", "BC")
}

abstract class DigestAlgorithm(val name: String, val provider: String?) {
    class Sha256 : DigestAlgorithm("SHA-256", null)
}

abstract class CipherAlgorithm(val name: String, val provider: String?) {
    class DesCbcPkcs5Padding : DigestAlgorithm("DES/CBC/PKCS5Padding", null) // EXAMPLE please don't actually DES. or triple DES
}

abstract class KeyAlgorithm(val name: String, val provider: String?, val keySpec: KeySpec) {
    class EcPrivatePairwise(ecPairwisePrivateKeySpec: EcPairwisePrivateKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwisePrivateKeySpec)
    class EcPublicPairwise(ecPairwisePublicKeySpec: EcPairwisePublicKeySpec) : KeyAlgorithm("ecPairwise", "DID", ecPairwisePublicKeySpec)
    class RSAPublic(keySpec: RSAPublicKeySpec) : KeyAlgorithm("RSA", null, keySpec)
    class Secp256k1Public(x: BigInteger, y: BigInteger): KeyAlgorithm("EC", null, ECPublicKeySpec(ECPoint(x, y), Curve.SECP256K1.toECParameterSpec()))
}

abstract class KeyGenAlgorithm(val name: String, val provider: String?, val spec: AlgorithmParameterSpec) {
    class Secp256k1 : KeyGenAlgorithm("EC", null, ECParameterSpec(
        EllipticCurve(
            ECFieldFp(SecP256K1Curve.q),
            SecP256K1Curve().a.toBigInteger(),
            SecP256K1Curve().b.toBigInteger()
        ),
        ECPoint(
            ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC).g.xCoord.toBigInteger(),
            ECNamedCurveTable.getParameterSpec(Constants.SECP256K1_CURVE_NAME_EC).g.yCoord.toBigInteger(),
        ),
        SecP256K1Curve().order,
        SecP256K1Curve().cofactor.toInt()
        ))
}

abstract class MacAlgorithm(val name: String, val provider: String?) {
    class Hmac512 : MacAlgorithm("HMAC512", null)
}