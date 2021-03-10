@file:UseSerializers(JwkSerializer::class)

package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.protocols.jose.serialization.JwkSerializer
import com.nimbusds.jose.jwk.JWK
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
data class RawIdentity(
    val id: String,
    val name: String,
    val keys: List<JWK>,
    val recoveryKey: String,
    val updateKey: String
)