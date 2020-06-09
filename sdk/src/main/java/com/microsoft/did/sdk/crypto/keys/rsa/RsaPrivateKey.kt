package com.microsoft.did.sdk.crypto.keys.rsa

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

class RsaPrivateKey(jwk: JsonWebKey) : PrivateKey(jwk) {
    override var kty = KeyType.RSA
    override var alg: String? = if (key.alg != null) key.alg!! else "RS256"

    val n = key.n
    val e = key.e
    val d = key.d
    val p = key.p
    val q = key.q
    val dp = key.dp
    val dq = key.dq
    val qi = key.qi
    val oth = key.oth

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = kty.value,
            alg = alg,
            kid = kid,
            key_ops = key_ops?.map { use -> use.value },
            use = use?.value,

            n = this.n,
            e = this.e,
            d = this.d,
            p = this.p,
            q = this.q,
            dp = this.dp,
            dq = this.dq,
            qi = this.qi,
            oth = this.oth
        )
    }

    override fun getPublicKey(): PublicKey {
        return RsaPublicKey(this.toJWK())
    }
}