// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.ClaimFormatter
import kotlinx.serialization.Serializable

/**
 * A VerifiableCredentialHolder holds a VerifiableCredential and additional meta-data like history and the display contract
 */
@Entity
@Serializable
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
data class VerifiableCredentialHolder(

    // id of the prime Verifiable Credential
    @PrimaryKey
    val cardId: String,

    // verifiable credential tied to Pairwise Identifier for Issuer.
    @Embedded
    val verifiableCredential: VerifiableCredential,

    @Embedded
    val owner: Identifier,

    val displayContract: DisplayContract
) {

    /**
     * Returns a ordered map containing a mapping of user readable claim label (not localized) to the formatted value.
     * e.g. the value of type date is formatted as a date instead of the raw timestamp.
     *
     * The order is adhering to the order of the claims within the display contract.
     *
     * Claims that are present in the VC but not in the DisplayContract or vice versa will not be contained.
     *
     * The display contract does not currently support localized claim labels.
     */
    fun getUserFormattedClaimMap(): LinkedHashMap<String, String> {
        val claimDescriptors = displayContract.claims
        val claimValues = verifiableCredential.contents.vc.credentialSubject

        val readableClaimMap = LinkedHashMap<String, String>()
        for ((claimIdentifier, claimDescriptor) in claimDescriptors) {
            val truncatedClaimIdentifier = if (claimIdentifier.startsWith("vc.credentialSubject.")) {
                claimIdentifier.removePrefix("vc.credentialSubject.")
            } else continue
            val claimValue = claimValues[truncatedClaimIdentifier] ?: continue
            val formattedClaimValue = ClaimFormatter.formatClaimValue(claimDescriptor.type, claimValue)
            readableClaimMap[claimDescriptor.label] = formattedClaimValue
        }
        return readableClaimMap
    }
}