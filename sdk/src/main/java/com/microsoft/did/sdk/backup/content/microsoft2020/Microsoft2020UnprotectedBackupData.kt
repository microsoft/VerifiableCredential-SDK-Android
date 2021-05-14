// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.backup.content.microsoft2020

import com.microsoft.did.sdk.backup.content.UnprotectedBackupData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @constructor
 * @param vcs map of VC identifier (JTI) to raw VC token data
 * @param vcsMetaInf map of VC Identifier (JTI) to VC metadata
 * @param metaInf backup metadata
 * @param identifiers List of raw identifiers.
 */
@Serializable
@SerialName(Microsoft2020UnprotectedBackupData.MICROSOFT_BACKUP_TYPE)
data class Microsoft2020UnprotectedBackupData(
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, VcMetadata>,
    val metaInf: WalletMetadata,
    val identifiers: List<RawIdentity>
) : UnprotectedBackupData() {
    override val type: String
        get() = MICROSOFT_BACKUP_TYPE

    companion object {
        const val MICROSOFT_BACKUP_TYPE = "MicrosoftWallet2020"
    }
}
