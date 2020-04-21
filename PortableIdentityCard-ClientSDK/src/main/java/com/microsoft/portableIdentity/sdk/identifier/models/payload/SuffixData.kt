package com.microsoft.portableIdentity.sdk.identifier.models.payload

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing payload for computing the unique suffix/identifier in short form. It comprises of hash of payload for patches
 * in multi hash format, public key of recovery key to be used for recovery/deactivation of operation on sidetree, commit/reveal value
 * to be used during the next recovery operation.
 */

@Serializable
data class SuffixData(
    @SerialName("patchDataHash")
    val patchDataHash: String,
    @SerialName("recoveryKey")
    val recoveryKey: JsonWebKey,
    @SerialName("nextRecoveryCommitmentHash")
    val nextRecoveryCommitmentHash: String
)