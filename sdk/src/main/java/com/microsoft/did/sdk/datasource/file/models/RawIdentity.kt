
@file:UseSerializers(JwkSerializer::class)
package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.serialization.JwkSerializer
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * @constructor
 * @param id Decentralized Identifier
 * @param name External label
 * @param alias Internal name
 * @param signatureKey Signing private key
 * @param encryptionKey Encrypting private key
 * @param recoveryKey private key used to recover
 * @param updateKey key used for DID updates
 */
@Serializable
data class RawIdentity (
    val id: String,
    val name: String,
    val keys: List<JWK>,
    val recoveryKey: String,
    val updateKey: String
) {
    companion object {
        suspend fun didToRawIdentifier(did: String, identityRepository: IdentifierRepository, keyStore: EncryptedKeyStore): RawIdentity? {
            return identityRepository.queryByIdentifier(did)?.let { identity ->
                val keys = listOf(
                    identity.encryptionKeyReference,
                    identity.signatureKeyReference,
                    identity.updateKeyReference,
                    identity.recoveryKeyReference
                ).mapNotNull { keyId ->
                    if (keyId.isNotBlank()) {
                        // due to field protections, must be cast out into an Map then back again
                        val jwk = keyStore.getKey(keyId).toJSONObject().toMutableMap()
                        if (keyId == identity.signatureKeyReference) {
                            jwk["use"] = KeyUse.SIGNATURE.identifier()
                        } else if (keyId == identity.encryptionKeyReference) {
                            jwk["use"] = KeyUse.ENCRYPTION.identifier()
                        }
                        JWK.parse(jwk)
                    } else {
                        null
                    }
                }
                RawIdentity(
                    id = identity.id,
                    name = identity.name,
                    keys = keys,
                    updateKey = identity.updateKeyReference,
                    recoveryKey = identity.recoveryKeyReference
                )
            }
        }
    }
}