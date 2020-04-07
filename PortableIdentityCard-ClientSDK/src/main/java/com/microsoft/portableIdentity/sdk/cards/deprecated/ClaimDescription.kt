package com.microsoft.portableIdentity.sdk.cards.deprecated

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class ClaimDescription(val header: String, val body: String) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null
}