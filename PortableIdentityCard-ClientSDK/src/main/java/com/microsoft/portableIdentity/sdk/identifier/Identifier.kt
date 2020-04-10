package com.microsoft.portableIdentity.sdk.identifier

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import kotlinx.serialization.Serializable

/**
 * Data model to describe a Portable Identifier (identifier document, key references, commit-reveal values for next operations)
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