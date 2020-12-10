package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import kotlinx.serialization.Serializable
import javax.inject.Inject

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
    val alias: String,
    val signatureKey: JsonWebKey?,
    val encryptionKey: JsonWebKey?,
    val recoveryKey: JsonWebKey,
    val updateKey: JsonWebKey
)