package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.identifier.document.RecoveryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuffixData(
    @SerialName("operationDataHash")
    val operationDataHash: String,
    @SerialName("recoveryKey")
    val recoveryKey: RecoveryKey,
    @SerialName("nextRecoveryOtpHash")
    val nextRecoveryCommitmentHash: String
) {}