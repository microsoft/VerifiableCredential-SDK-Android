// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import java.security.KeyPair
import java.security.interfaces.ECPublicKey

/**
 * Returns a Nimbus private JWK built from Java key material. Currently hardcoded to SECP256k1.
 */
fun KeyPair.toPrivateJwk(keyId: String, keyUse: KeyUse): JWK {
    return ECKey.Builder(Curve.SECP256K1, public as ECPublicKey).privateKey(private).keyID(keyId).keyUse(keyUse).build()
}

/**
 * Returns a Nimbus public JWK built from Java key material. Currently hardcoded to SECP256k1.
 */
fun KeyPair.toPublicJwk(keyId: String): JWK {
    return ECKey.Builder(Curve.SECP256K1, public as ECPublicKey).keyID(keyId).build()
}
