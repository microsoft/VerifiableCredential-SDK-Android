package com.microsoft.portableIdentity.sdk.identifier.models

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.RecoveryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuffixData(
    @SerialName("patchDataHash")
    val operationDataHash: String,
    @SerialName("recoveryKey")
    val recoveryKey: JsonWebKey,
    @SerialName("nextRecoveryCommitmentHash")
    val nextRecoveryCommitmentHash: String
) {}