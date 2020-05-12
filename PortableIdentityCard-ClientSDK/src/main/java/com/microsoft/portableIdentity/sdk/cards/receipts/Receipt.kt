package com.microsoft.portableIdentity.sdk.cards.receipts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Receipt (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // did of the verifier/issuer
    val entityIdentifier: String,

    // date action occurred
    val activityDate: Long,

    //Name of the verifier/issuer
    val entityName: String,

    val cardId: String
)