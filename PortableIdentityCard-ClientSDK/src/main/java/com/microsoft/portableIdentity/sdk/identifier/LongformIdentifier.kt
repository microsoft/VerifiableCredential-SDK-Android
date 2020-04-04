package com.microsoft.portableIdentity.sdk.identifier

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import kotlinx.serialization.Serializable

/**
 * Data model to describe a Portable Identity Card.
 */
@Entity
@Serializable
data class LongformIdentifier (

    @PrimaryKey
    val identifier: String,
    val alias: String,
    val nextUpdateCommitmentHash: String,
    val document: IdentifierDocument

)