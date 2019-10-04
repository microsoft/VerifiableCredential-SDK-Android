package com.microsoft.did.sdk.utilities

import android.util.Base64
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
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
                        n = Base64.encodeToString((publicKey as RSAPublicKey).modulus.toByteArray(), Base64.URL_SAFE),
                        e = Base64.encodeToString(publicKey.publicExponent.toByteArray(), Base64.URL_SAFE)
                    )
                )
            }
            KeyType.EllipticCurve -> {
                EllipticCurvePublicKey(
                    JsonWebKey(
                        kty = KeyType.EllipticCurve.value,
                        kid = alias,
                        x = Base64.encodeToString((publicKey as ECPublicKey).w.affineX.toByteArray(), Base64.URL_SAFE),
                        y = Base64.encodeToString(publicKey.w.affineY.toByteArray(), Base64.URL_SAFE)
                    )
                )
            }
            else -> throw Error("Cannot convert key type.")
        }
    }

    fun androidPrivateKeyToPrivateKey(alias: String, privateKey: KeyStore.PrivateKeyEntry): PrivateKey {
        return when (whatKeyTypeIs(privateKey.certificate.publicKey)) {
            KeyType.RSA -> {
                val key = privateKey.certificate.publicKey
                RsaPrivateKey (
                    JsonWebKey(
                        kty = KeyType.RSA.value,
                        kid = alias,
                        n = Base64.encodeToString((key as RSAPublicKey).modulus.toByteArray(), Base64.URL_SAFE),
                        e = Base64.encodeToString(key.publicExponent.toByteArray(), Base64.URL_SAFE),
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
                val key = privateKey.certificate.publicKey
                EllipticCurvePrivateKey (
                    JsonWebKey(
                        kty = KeyType.EllipticCurve.value,
                        kid = alias,
                        x = Base64.encodeToString((key as ECPublicKey).w.affineX.toByteArray(), Base64.URL_SAFE),
                        y = Base64.encodeToString(key.w.affineY.toByteArray(), Base64.URL_SAFE),
                        d = "0"
                    )
                )
            }
            else -> throw Error("Cannot convert key type.")
        }
    }

    fun androidSecretKeyToSecretKey(alias: String, secretKey: KeyStore.SecretKeyEntry): SecretKey {
        return SecretKey(
            JsonWebKey(
                kty = KeyType.Octets.value,
                kid = alias,
                k = Base64.encodeToString(secretKey.secretKey.encoded, Base64.URL_SAFE)
            )
        )
    }

    fun whatKeyTypeIs(publicKey: java.security.PublicKey): KeyType {
        return when (publicKey) {
            is RSAPublicKey -> KeyType.RSA
            is ECPublicKey -> KeyType.EllipticCurve
            else -> throw Error("Unknown Key Type")
        }
    }
}