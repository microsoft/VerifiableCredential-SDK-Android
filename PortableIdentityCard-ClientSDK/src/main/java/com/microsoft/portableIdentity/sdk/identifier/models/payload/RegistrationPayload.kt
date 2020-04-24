package com.microsoft.portableIdentity.sdk.identifier.models.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing payload for type of operation performed on sidetree (create/update/recover/deactivate) along with
 * payload for generating unique suffix/short form identifier and list of patches to be performed on identifier document.
 */

@Serializable
data class RegistrationPayload (
/*    @SerialName("type")
    val type:String,*/
    @SerialName("suffixData")
    val suffixData: String,
    @SerialName("patchData")
    val patchData: String
)