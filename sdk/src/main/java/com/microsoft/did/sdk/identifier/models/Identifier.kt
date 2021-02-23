package com.microsoft.did.sdk.identifier.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Data class representing identifier to be stored in database along with its properties in identifier document
 */
@Entity
@Serializable
data class Identifier(
    @PrimaryKey
    val id: String,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val recoveryKeyReference: String,
    val updateKeyReference: String,
    val name: String
)
