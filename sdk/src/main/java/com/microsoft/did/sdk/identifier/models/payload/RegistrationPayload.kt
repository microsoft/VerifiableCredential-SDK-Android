package com.microsoft.did.sdk.identifier.models.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing payload for type of operation performed on sidetree (create/update/recover/deactivate) along with
 * payload for generating unique suffix/short form identifier and list of patches to be performed on identifier document.
 */

@Serializable
data class RegistrationPayload(
    @SerialName("suffixData")
    val suffixData: SuffixData,
    @SerialName("delta")
    val patchData: PatchData
)