package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.identifier.OperationData
import com.microsoft.portableIdentity.sdk.identifier.SuffixData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegDoc (
    @SerialName("type")
    val type:String,
    @SerialName("suffixData")
    val suffixData: String,
    @SerialName("operationData")
    val operationData: String
) {}