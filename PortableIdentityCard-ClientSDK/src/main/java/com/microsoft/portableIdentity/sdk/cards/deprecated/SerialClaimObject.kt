package com.microsoft.portableIdentity.sdk.cards.deprecated

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
@Deprecated("Born into deprecation ;-(")
data class SerialClaimObject(val serialClaimObject: String) {
    @PrimaryKey(autoGenerate = true) var uid:Int = 0
}