package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.models.Identifier
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
import java.security.KeyException
import javax.inject.Inject

/**
 * @constructor
 * @param vcs map of VC identifier (JTI) to raw VC token data
 * @param vcsMetaInf map of VC Identifier (JTI) to VC metadata
 * @param metaInf backup metadata
 * @param identifiers List of raw identifiers.
 */
@Serializable
@SerialName(MicrosoftUnprotectedBackup2020.MICROSOFT_BACKUP_TYPE)
class MicrosoftUnprotectedBackup2020 (
    val vcs: Map<String, String>,
    val vcsMetaInf: Map<String, VCMetadata>,
    val metaInf: WalletMetadata,
    val identifiers: List<RawIdentity>
) : UnprotectedBackup() {
    @Transient
    private lateinit var metadataCallback: suspend (WalletMetadata) -> Unit
    @Transient
    private lateinit var verifiableCredentialCallback: suspend (VerifiableCredential, VCMetadata) -> Unit
    @Transient
    @Inject
    internal lateinit var jsonSerializer: Json
    @Transient
    @Inject
    internal lateinit var identityRepository: IdentifierRepository
    @Transient
    @Inject
    internal lateinit var keyStore: EncryptedKeyStore

    override val type: String
        get() = MICROSOFT_BACKUP_TYPE

    companion object {
        const val MICROSOFT_BACKUP_TYPE = "MicrosoftWallet2020"

        suspend fun build(
            metadata: WalletMetadata,
            verifiableCredentials: List<Pair<VerifiableCredential, VCMetadata>>,
            identityRepository: IdentifierRepository,
            keyStore: EncryptedKeyStore): MicrosoftUnprotectedBackup2020 {

            val vcMap = mutableMapOf<String, String>()
            val vcMetaMap = mutableMapOf<String, VCMetadata>()
            val ownedDids = mutableListOf<String>()

            verifiableCredentials.forEach { vcPair ->
                val jti = vcPair.first.jti
                vcMap[jti] = vcPair.first.raw
                vcMetaMap[jti] = vcPair.second
                ownedDids.add(vcPair.first.contents.sub)
            }

            val identifiers = ownedDids.mapNotNull {
                identityRepository.queryByIdentifier(it)?.let { identity ->
                    val keys = listOf(
                        identity.encryptionKeyReference,
                        identity.signatureKeyReference,
                        identity.updateKeyReference,
                        identity.recoveryKeyReference
                    ).mapNotNull { keyId ->
                        if (keyId.isNotBlank()) {
                            keyStore.getKey(keyId)
                        } else {
                            null
                        }
                    }
                    RawIdentity(
                        identity.id,
                        name = identity.name,
                        keys
                    )
                }
            }

            return MicrosoftUnprotectedBackup2020(
                metaInf = metadata,
                vcs = vcMap,
                vcsMetaInf = vcMetaMap,
                identifiers = identifiers
            )
        }
    }

    fun initialize(
        walletMetadataCallback: suspend (WalletMetadata) -> Unit,
        verifiableCredentialCallback:  suspend (VerifiableCredential, VCMetadata) -> Unit) {
        this.metadataCallback = walletMetadataCallback
        this.verifiableCredentialCallback = verifiableCredentialCallback
    }

    override suspend fun import(): Result<Unit> {
        try {
            this.identifiers.forEach { raw -> restoreIdentifier(raw) }
        } catch (exception: SdkException) {
            return Result.Failure(exception)
        } catch (exception: Exception) {
            return Result.Failure(MalformedIdentity("unhandled exception thrown", exception))
        }
        this.vcsToIterator(jsonSerializer).forEach {
                dataPair ->
            // TODO: Some validation that the VC is issued to an identifier under our control
            try {
                verifiableCredentialCallback(dataPair.first, dataPair.second)
            } catch (exception: SdkException) {
                return Result.Failure(exception)
            } catch (exception: Exception) {
                return Result.Failure(MalformedVerifiableCredential("unhandled exception thrown", exception))
            }
        }
        try {
            metadataCallback(this.metaInf)
        } catch (exception: SdkException) {
            return Result.Failure(exception)
        } catch (exception: Exception) {
            return Result.Failure(MalformedMetadata("unhandled exception thrown", exception))
        }
        return Result.Success(Unit)
    }

    private suspend fun restoreIdentifier (
        identifierData: RawIdentity
    ): Identifier {
        var signingKeyRef: String = ""
        var encryptingKeyRef: String = ""
        var recoveryKeyRef: String = ""
        var updateKeyRef: String = ""
        for (key in identifierData.keys) {
            if (signingKeyRef.isBlank() &&
                (key.keyOperations?.any { listOf(KeyOperation.SIGN, KeyOperation.VERIFY).contains(it) } == true ||
                    key.keyUse == KeyUse.SIGNATURE)) {
                signingKeyRef = importKey(key)
            }
            if (encryptingKeyRef.isBlank() &&
                (key.keyOperations?.containsAll(listOf(KeyOperation.ENCRYPT, KeyOperation.DECRYPT)) == true ||
                    key.keyOperations?.containsAll(listOf(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY)) == true ||
                    key.keyUse == KeyUse.ENCRYPTION)) {
                encryptingKeyRef = importKey(key)
            }
        }
        if (updateKeyRef.isEmpty() || recoveryKeyRef.isBlank()) {
            throw MalformedIdentity("update and recovery key required")
        }
        val id = Identifier(
            identifierData.id,
            signingKeyRef,
            encryptingKeyRef,
            recoveryKeyRef,
            updateKeyRef,
            identifierData.name
        )
        identityRepository.insert(id)
        return id
    }

    private fun importKey(
        jwk: JWK
    ): String {
        val kid = jwk.keyID ?: throw com.microsoft.did.sdk.util.controlflow.KeyException("Imported JWK has no key id.")
        if (!keyStore.containsKey(kid)) {
            keyStore.storeKey(jwk, kid)
        }
        return kid
    }


    fun vcsToIterator(serializer: Json): Iterator<Pair<VerifiableCredential, VCMetadata>> {
        return VCIterator(vcs, vcsMetaInf, serializer)
    }

    private class VCIterator (
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
            val verifiableCredentialContent = serializer.decodeFromString(VerifiableCredentialContent.serializer(), rawToken)
            val vc = VerifiableCredential(verifiableCredentialContent.jti, rawToken, verifiableCredentialContent)
            return Pair(vc, vcsMetaInf[jti]!!)
        }
    }
}
