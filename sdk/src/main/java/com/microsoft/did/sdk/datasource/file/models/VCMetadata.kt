package com.microsoft.did.sdk.datasource.file.models

import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract

interface VCMetadata {
    val type: String
    val displayContract: DisplayContract
}
