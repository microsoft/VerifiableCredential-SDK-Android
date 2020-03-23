package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OperationData (
    @SerialName("nextUpdateOtpHash")
    val nextUpdateOtpHash: String,
    @SerialName("document")
    val regDocument: IdentifierDoc
) {}