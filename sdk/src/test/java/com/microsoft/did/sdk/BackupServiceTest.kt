// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.backup.model.BackupData
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import org.junit.Test

class BackupServiceTest {

    private val backupService = BackupService(Serializer())



    @Test
    fun `expected backup string`() {
        val vcs = HashMap<VerifiableCredential, Map<String, String>>()
        val metaData = HashMap<String, String>()
        val identifier = emptyList<Identifier>()
        val backupData = BackupData(vcs, metaData, identifier)

        val actualResult = backupService.createBackup(backupData)

        println(actualResult)
    }
}