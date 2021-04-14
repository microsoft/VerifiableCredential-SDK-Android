package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.MalformedIdentity
import com.microsoft.did.sdk.util.controlflow.MalformedMetadata
import com.microsoft.did.sdk.util.controlflow.MalformedVerifiableCredential
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.KeyOperation
import com.nimbusds.jose.jwk.KeyUse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

/**
 * @constructor
 * @param vcs map of VC identifier (JTI) to raw VC token data
 * @param vcsMetaInf map of VC Identifier (JTI) to VC metadata
 * @param metaInf backup metadata
 * @param identifiers List of raw identifiers.
 */
@Serializable
@SerialName(MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE)
class MicrosoftUnprotectedBackup2020(
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, VCMetadata>,
    val metaInf: WalletMetadata,
    val identifiers: List<RawIdentity>
) : UnprotectedBackup() {
    override val type: String
        get() = MICROSOFT_BACKUP_TYPE

    companion object {
        const val MICROSOFT_BACKUP_TYPE = "MicrosoftWallet2020"
    }

    internal fun vcsToIterator(serializer: Json): Iterator<Pair<VerifiableCredential, VCMetadata>> {
        return MicrosoftUnprotectedBackup2020.VCIterator(vcs, vcsMetaInf, serializer)
    }

    private class VCIterator(
        val vcs: Map<String, String>,
        val vcsMetaInf: Map<String, VCMetadata>,
        val serializer: Json
    ) : Iterator<Pair<VerifiableCredential, VCMetadata>> {
        val jtis: Iterator<String> = vcs.keys.iterator()

        override fun hasNext(): Boolean {
            return jtis.hasNext()
        }

        override fun next(): Pair<VerifiableCredential, VCMetadata> {
            val jti = jtis.next()
            val rawToken = vcs[jti]!!
            val jwsToken = JwsToken.deserialize(rawToken)
            val verifiableCredentialContent = serializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
            val vc = VerifiableCredential(verifiableCredentialContent.jti, rawToken, verifiableCredentialContent)
            return Pair(vc, vcsMetaInf[jti]!!)
        }
    }
}
