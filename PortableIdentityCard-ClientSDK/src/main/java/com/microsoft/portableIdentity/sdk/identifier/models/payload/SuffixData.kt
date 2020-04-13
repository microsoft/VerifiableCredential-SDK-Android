package com.microsoft.portableIdentity.sdk.identifier.models.payload

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuffixData(
    @SerialName("patchDataHash")
    val patchDataHash: String,
    @SerialName("recoveryKey")
    val recoveryKey: JsonWebKey,
    @SerialName("nextRecoveryCommitmentHash")
    val nextRecoveryCommitmentHash: String
)