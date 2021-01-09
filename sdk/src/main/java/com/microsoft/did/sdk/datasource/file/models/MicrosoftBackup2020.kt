package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.util.formVerifiableCredential
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
    val vcsMetaInf: Map<String, VCMetadata>,
    val metaInf: WalletMetadata,
    val identifiers: List<RawIdentity>
) {
    val type = "MicrosoftWallet2020"

    fun vcsToIterator(): Iterator<Pair<VerifiableCredential, VCMetadata>> {
        return VCIterator(vcs, vcsMetaInf)
    }

    private class VCIterator: Iterator<Pair<VerifiableCredential, VCMetadata>> constructor (
        val vcs: Map<String, String>,
        val vcsMetaInf: Map<String, VCMetadata>
    ) {
        val jtis: Iterator<String> = vcs.keys.iterator()

        override fun hasNext(): Boolean {
            return jtis.hasNext()
        }

        override fun next(): Pair<VerifiableCredential, VCMetadata> {
            val jti = jtis.next()
            return Pair(formVerifiableCredential(vcs[jti]), vcsMetaInf[jti])
        }
    }
}
