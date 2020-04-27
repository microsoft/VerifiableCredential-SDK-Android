package com.microsoft.portableIdentity.sdk.cards.receipts

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReceiptAction(action: String) {
    Issuance("issuance"),
    Presentation("presentation")
}

@Entity
data class Receipt (

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val action: ReceiptAction,

    val token: String,

    // did of the verifier/issuer
    val entityIdentifier: String,

    // date action occurred
    val activityDate: Long,

    // Host name of verifier/issuer
    val entityHostName: String,

    val cardId: String
)