package com.microsoft.portableIdentity.sdk.registrars

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationDocument (
    @SerialName("type")
    val type:String,
    @SerialName("suffixData")
    val suffixData: String,
    @SerialName("patchData")
    val patchData: String
) {}