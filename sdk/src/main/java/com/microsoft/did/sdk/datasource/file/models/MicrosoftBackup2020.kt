package com.microsoft.did.sdk.datasource.file.models

import kotlinx.serialization.Serializable

/**
 * @constructor
 * @param vcs map of VC identifier (JTI) to raw VC token data
 * @param vcsMetaInf map of VC Identifier (JTI) to VC metadata
 * @param metaInf backup metadata
 * @param identifiers List of raw identifiers.
 */
@Serializable
data class MicrosoftBackup2020 (
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, Map<String, Any>>,
    val metaInf: Map<String, Any>,
    val identifiers: List<RawIdentity>
) {
    val type = "MicrosoftWallet2020"
}