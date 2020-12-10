package com.microsoft.did.sdk.datasource.file

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import javax.inject.Inject

class BackupOperation @Inject constructor (
    private val identifierRepository: IdentifierRepository,
    private val cryptoOperations: CryptoOperations,
) {

}