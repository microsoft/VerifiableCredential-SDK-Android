package com.microsoft.portableIdentity.sdk.cards.receipts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
enum class ReceiptAction {
    Issuance,
    Presentation
}

@Entity
data class Receipt (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @Embedded
    val action: ReceiptAction,

    val token: String,

    // did of the verifier/issuer
    val entity: String,

    // date action occurred
    val activityDate: Long,

    val cardId: String
)