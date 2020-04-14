package com.microsoft.portableIdentity.sdk.identifier

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data class representing identifier to be stored in database along with its properties in identifier document
 */

@Entity
@Serializable
data class Identifier (

    @PrimaryKey
    val id: String,
    val alias: String,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val recoveryKeyReference: String,
    val nextUpdateCommitmentHash: String,
    val nextRecoveryCommitmentHash: String,
    //val document: IdentifierDocument,
    val name: String
)