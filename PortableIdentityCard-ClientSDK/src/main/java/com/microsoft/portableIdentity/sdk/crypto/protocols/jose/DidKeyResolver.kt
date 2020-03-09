package com.microsoft.portableIdentity.sdk.crypto.protocols.jose

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger

object DidKeyResolver {
    suspend fun resolveIdentiferFromKid(kid: String, crypto: CryptoOperations, resolver: IResolver, logger: ILogger): Identifier {
        val did = Regex("^([^#]+)#.+$").matchEntire(kid) ?: throw logger.error("No identifier found in key id")
        return resolver.resolve(did.groupValues[1], crypto)
    }

    suspend fun resolveKeyFromKid(kid: String, crypto: CryptoOperations, resolver: IResolver, logger: ILogger): PublicKey {
        val identifier = resolveIdentiferFromKid(kid, crypto, resolver, logger = logger)
        val did = Regex("^[^#]+(#.+)$").matchEntire(kid)!!
        return identifier.document.publicKeys.filter {
            it.publicKeyJwk.kid?.endsWith(did.groupValues[1]) ?: false ||
                    it.id.endsWith(did.groupValues[1])
        }.firstOrNull()?.toPublicKey(logger = logger) ?: throw logger.error("Could not find key $kid")
    }
    
    suspend fun verifyJws(jws: JwsToken, crypto: CryptoOperations, forDid: Identifier, logger: ILogger) {
        val keys = forDid.document.publicKeys.map {
            it.toPublicKey(logger = logger)
        }
        jws.verify(crypto, keys)
    }

    suspend fun verifyJws(jws: JwsToken, crypto: CryptoOperations, resolver: IResolver, forDid: String? = null, logger: ILogger) {
        if (forDid.isNullOrBlank()) {
            val sender = resolver.resolve(forDid!!, crypto)
            // verify the request
            verifyJws(jws, crypto, sender, logger = logger)
        } else {
            val keys = mutableListOf<PublicKey>()
            jws.signatures.forEachIndexed { index, signature ->
                val kid = signature.getKid(logger = logger) ?: throw logger.error("Could not find kid in signature $index")
                keys.add(resolveKeyFromKid(kid, crypto, resolver, logger))
            }
            jws.verify(crypto, keys)
        }
    }
}