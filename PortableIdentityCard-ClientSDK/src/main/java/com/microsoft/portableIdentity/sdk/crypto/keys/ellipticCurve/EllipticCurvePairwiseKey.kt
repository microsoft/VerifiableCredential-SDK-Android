package com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.Secp256k1Provider
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import org.bitcoin.NativeSecp256k1
import java.math.BigInteger

class EllipticCurvePairwiseKey {

    companion object {
        private val supportedCurves = listOf("K-256", "P-256K")
        fun generate(crypto: CryptoOperations, personaMasterKey: ByteArray, algorithm: EcKeyGenParams, peerId: String): PrivateKey {
            val crypto: SubtleCrypto =
                crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private);
            // Generate the master key
            val alg: Algorithm =
                EcdsaParams(
                    hash = Sha.Sha256
                )
            val signingKey = JsonWebKey(
                kty = KeyType.Octets.value,
                alg = JoseConstants.Hs256.value,
                k = Base64Url.encode(personaMasterKey)
            )
            val key = crypto.importKey(
                KeyFormat.Jwk, signingKey, alg, false, listOf(
                    KeyUsage.Sign
                )
            )
            val pairwiseKeySeed = crypto.sign(alg, key, peerId.map { it.toByte() }.toByteArray())
            if (supportedCurves.indexOf(algorithm.namedCurve) == -1)
                throw SdkLog.error("Curve ${algorithm.namedCurve} is not supported")
            
            val pair = NativeSecp256k1.computePubkey(pairwiseKeySeed)
            val keyPair = CryptoKeyPair(
                privateKey = CryptoKey(
                    com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyType.Private,
                    false,
                    JwaCryptoConverter.jwkAlgToKeyGenWebCrypto(algorithm.namedCurve),
                    listOf(KeyUsage.Sign),
                    Secp256k1Provider.Secp256k1Handle("", pairwiseKeySeed)
                ),
                publicKey = CryptoKey(
                    com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyType.Public,
                    true,
                    JwaCryptoConverter.jwkAlgToKeyGenWebCrypto(algorithm.namedCurve),
                    listOf(KeyUsage.Verify),
                    Secp256k1Provider.Secp256k1Handle("", pair)
                )
            )
            return EllipticCurvePrivateKey(crypto.exportKeyJwk(keyPair.privateKey))
        }
    }
}