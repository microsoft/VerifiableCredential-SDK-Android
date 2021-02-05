package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.datasource.file.models.BackupFile
import com.microsoft.did.sdk.datasource.file.models.MicrosoftBackup2020
import com.microsoft.did.sdk.datasource.file.models.VCMetadata
import com.microsoft.did.sdk.datasource.file.models.WalletMetadata
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import javax.inject.Inject

typealias MetadataInflator = () -> WalletMetadata
typealias VerifiableCredentialMetadataInflator = (verifiableCredential: VerifiableCredential) -> VCMetadata

class BackupOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val cryptoOperations: CryptoOperations,
) {
    private lateinit var metadataInflator: MetadataInflator
    private lateinit var verifiableCredentialMetadataInflator: VerifiableCredentialMetadataInflator
    private var backup: BackupFile? = null

    fun initialize(metadataInflator: MetadataInflator, verifiableCredentialMetadataInflator: VerifiableCredentialMetadataInflator) {
        this.metadataInflator = metadataInflator
        this.verifiableCredentialMetadataInflator = verifiableCredentialMetadataInflator
    }

    fun createBackup(type: String = MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE): BackupFile {
        when (type) {
            MicrosoftBackup2020.MICROSOFT_BACKUP_TYPE -> {
                createMicrosoftBackup()
            }
            else -> {
                throw AlgorithmException("Unknown backup type $type")
            }
        }
        return this.backup!!
    }

    private fun createMicrosoftBackup() {
//        this.backup = MicrosoftBackup2020(
//            vcs,
//
//        )
    }
}