package com.microsoft.did.sdk.credentials

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class ClaimDescription(val header: String, val body: String, val claimObjectUid: String) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null
}