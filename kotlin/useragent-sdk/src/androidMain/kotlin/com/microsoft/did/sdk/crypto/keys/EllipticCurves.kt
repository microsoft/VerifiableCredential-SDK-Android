package com.microsoft.did.sdk.crypto.keys

import java.math.BigInteger
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve

// We'll come back to this later but this is for native curves. It may not be necessary.
enum class EllipticCurves(spec: ECParameterSpec) {
    secp256k1(ECParameterSpec(
        EllipticCurve(
            ECFieldFp(
                BigInteger(
                    "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFE" +
                            "FFFFFC2F", 16
                )
            ),
            BigInteger("00000000000000000000000000000000000000000000000000000000" +
                    "00000000", 16),
            BigInteger("00000000000000000000000000000000000000000000000000000000" +
                    "00000007", 16)
        ),
        ECPoint(
            BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0" +
                    "F4A13945D898C296", 16),
            BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE3357" +
                    "6B315ECECBB6406837BF51F5", 16)
        ),
        BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2" +
                "FC632551", 16),
        0x01
    ))
}