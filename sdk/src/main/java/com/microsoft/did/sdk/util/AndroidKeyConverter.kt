// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import android.util.Base64
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.util.controlflow.KeyException
import java.security.KeyStore
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

object AndroidKeyConverter {
    fun androidPublicKeyToPublicKey(alias: String, publicKey: java.security.PublicKey): PublicKey {
        return when (whatKeyTypeIs(publicKey)) {
            KeyType.RSA -> {
                RsaPublicKey(
                    JsonWebKey(
                        kty = KeyType.RSA.value,
                        kid = alias,
                        alg = publicKey.algorithm,
                        key_ops = listOf(KeyUsage.Encrypt.value),
                        use = KeyUse.Encryption.value,
                        n = Base64.encodeToString(
                            (publicKey as RSAPublicKey).modulus.toByteArray(),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ).trim(),
                        e = Base64.encodeToString(
                            publicKey.publicExponent.toByteArray(),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ).trim()
                    )
                )
            }
            KeyType.EllipticCurve -> {
                EllipticCurvePublicKey(
                    JsonWebKey(
                        kty = KeyType.EllipticCurve.value,
                        kid = alias,
                        alg = publicKey.algorithm,
                        key_ops = listOf(KeyUsage.Verify.value),
                        use = KeyUse.Signature.value,
                        x = Base64.encodeToString(
                            (publicKey as ECPublicKey).w.affineX.toByteArray(),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ).trim(),
                        y = Base64.encodeToString(
                            publicKey.w.affineY.toByteArray(),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ).trim()
                    )
                )
            }
            else -> throw KeyException("Cannot convert key type.")
        }
    }

    fun androidPrivateKeyToPrivateKey(alias: String, keyStore: KeyStore): PrivateKey {
        val key = keyStore.getCertificate(alias).publicKey
        return when (whatKeyTypeIs(key)) {
            KeyType.RSA -> {
                RsaPrivateKey(
                    JsonWebKey(
                        kty = KeyType.RSA.value,
                        kid = alias,
                        alg = key.algorithm,
                        key_ops = listOf(KeyUsage.Decrypt.value),
                        use = KeyUse.Encryption.value,
                        n = Base64.encodeToString(
                            (key as RSAPublicKey).modulus.toByteArray(),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ),
                        e = Base64.encodeToString(key.publicExponent.toByteArray(), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                        d = "0",
                        p = "0",
                        q = "0",
                        dp = "0",
                        dq = "0",
                        qi = "0"
                    )
                )
            }
            KeyType.EllipticCurve -> {
                EllipticCurvePrivateKey(
                    JsonWebKey(
                        kty = KeyType.EllipticCurve.value,
                        kid = alias,
                        alg = key.algorithm,
                        key_ops = listOf(KeyUsage.Sign.value),
                        use = KeyUse.Signature.value,
                        x = Base64.encodeToString(
                            (key as ECPublicKey).w.affineX.toByteArray(),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ),
                        y = Base64.encodeToString(key.w.affineY.toByteArray(), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                        d = "0"
                    )
                )
            }
            else -> throw KeyException("Cannot convert key type.")
        }
    }

    fun whatKeyTypeIs(publicKey: java.security.PublicKey): KeyType {
        return when (publicKey) {
            is RSAPublicKey -> KeyType.RSA
            is ECPublicKey -> KeyType.EllipticCurve
            else -> throw KeyException("Unknown Key Type")
        }
    }
}