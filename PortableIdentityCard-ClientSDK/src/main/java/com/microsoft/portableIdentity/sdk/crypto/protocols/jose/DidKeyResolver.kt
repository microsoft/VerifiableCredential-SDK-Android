package com.microsoft.portableIdentity.sdk.crypto.protocols.jose

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result

object DidKeyResolver {
    //TODO Replace error with exception something generic related to Keys
    private suspend fun resolveIdentifierFromKid(kid: String, crypto: CryptoOperations, resolver: Resolver): Result<IdentifierDocument> {
        val did = Regex("^([^#]+)#.+$").matchEntire(kid) ?: throw SdkLog.error("No identifier found in key id")
        return when (val identifierDocument = resolver.resolve(did.groupValues[1])) {
            is Result.Success -> Result.Success(identifierDocument.payload)
            is Result.Failure -> identifierDocument
        }
    }

    private suspend fun resolveKeyFromKid(kid: String, crypto: CryptoOperations, resolver: Resolver): Result<PublicKey> {
        val did = Regex("^[^#]+(#.+)$").matchEntire(kid)!!
        return when (val identifierDocument = resolveIdentifierFromKid(kid, crypto, resolver)) {
            is Result.Success -> Result.Success(identifierDocument.payload.publicKey.firstOrNull {
                it.publicKeyJwk.kid?.endsWith(did.groupValues[1]) ?: false ||
                        it.id.endsWith(did.groupValues[1])
            }?.toPublicKey() ?: throw SdkLog.error("Could not find key $kid"))
            is Result.Failure -> identifierDocument
        }
    }

    private suspend fun verifyJws(jws: JwsToken, crypto: CryptoOperations, identifierDocument: IdentifierDocument) {
        val keys = identifierDocument.publicKey.map {
            it.toPublicKey()
        }
        jws.verify(crypto, keys)
    }

    suspend fun verifyJws(jws: JwsToken, crypto: CryptoOperations, resolver: Resolver, forDid: String? = null) {
        if (forDid.isNullOrBlank()) {
            // verify the request
            when (val sender = resolver.resolve(forDid!!)) {
                is Result.Success -> verifyJws(jws, crypto, sender.payload)
            }
        } else {
            val keys = mutableListOf<PublicKey>()
            jws.signatures.forEachIndexed { index, signature ->
                val kid = signature.getKid() ?: throw SdkLog.error("Could not find kid in signature $index")
                when(val key = resolveKeyFromKid(kid, crypto, resolver)) {
                    is Result.Success -> keys.add(key.payload)
                }

            }
            jws.verify(crypto, keys)
        }
    }
}