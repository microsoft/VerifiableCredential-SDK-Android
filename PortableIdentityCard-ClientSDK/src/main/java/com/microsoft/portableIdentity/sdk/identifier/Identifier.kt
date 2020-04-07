package com.microsoft.portableIdentity.sdk.identifier

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.registrars.Registrar
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import kotlinx.serialization.Serializable

/**
 * Data model to describe a Portable Identity Card.
 */
@Entity
@Serializable
data class Identifier (

    @PrimaryKey
    val identifier: String,
    val alias: String,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val recoveryKeyReference: String,
    val nextUpdateCommitmentHash: String,
    val nextRecoveryCommitmentHash: String,
    val document: IdentifierDocument,
    val name: String
)