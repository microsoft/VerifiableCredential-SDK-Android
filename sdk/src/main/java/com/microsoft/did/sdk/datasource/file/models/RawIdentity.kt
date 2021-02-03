package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.Serializable

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
    val keys: List<JsonWebKey>
)