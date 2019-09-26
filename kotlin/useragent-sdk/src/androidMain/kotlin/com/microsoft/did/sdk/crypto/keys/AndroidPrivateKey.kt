package com.microsoft.did.sdk.crypto.keys

import android.util.Base64
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import java.security.KeyStore
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

class AndroidPrivateKey private constructor (jwk: JsonWebKey): PrivateKey(jwk) {

    constructor(alias: String, kty: KeyType, entry: KeyStore.PrivateKeyEntry): this(
        when(kty) {
            KeyType.RSA -> {
                JsonWebKey(
                    kty = KeyType.RSA.value,
                    kid = alias,
                    n = Base64.encodeToString((entry.certificate.publicKey as RSAPublicKey).modulus.toByteArray(), Base64.URL_SAFE),
                    e = Base64.encodeToString((entry.certificate.publicKey as RSAPublicKey).publicExponent.toByteArray(), Base64.URL_SAFE)
                )
            }
            KeyType.EllipticCurve -> {
                JsonWebKey(
                    kty = KeyType.EllipticCurve.value,
                    kid = alias,
                    x = Base64.encodeToString((entry.certificate.publicKey as ECPublicKey).w.affineX.toByteArray(), Base64.URL_SAFE),
                    y = Base64.encodeToString((entry.certificate.publicKey as ECPublicKey).w.affineY.toByteArray(), Base64.URL_SAFE)
                )
            }
            else -> {
                throw Error("Cannot convert key type ${kty.value}")
            }
        }
    )

    override fun getPublicKey(): PublicKey {
        return RsaPublicKey(
            JsonWebKey(
                kty = this.kty.value,
                alg = this.alg
        ))
    }
}