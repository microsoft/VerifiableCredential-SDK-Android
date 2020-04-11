package com.microsoft.portableIdentity.sdk.identifier

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument

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