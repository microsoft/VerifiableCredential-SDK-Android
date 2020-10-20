// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.backup.model.BackupData
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    private val serializer: Serializer
) {

    companion object {
        const val VERIFIABLE_CREDENTIALS = "vcs"
        const val VERIFIABLE_CREDENTIAL_META_DATA = "vcsMetaData"
        const val COMMON_META_DATA = "metaData"
        const val IDENTIFIERS = "identifiers"
    }

    fun createBackup(backupData: BackupData): String {
        val base = HashMap<String, Any>()
        base[VERIFIABLE_CREDENTIALS] = createVcs(backupData)
        base[VERIFIABLE_CREDENTIAL_META_DATA] = createVcsMetaData(backupData)
        base[COMMON_META_DATA] = backupData.metaData
        base[IDENTIFIERS] = emptyList<Identifier>()
        serializer.stringify(base, String::class, Any::class)
        val t = HashMap<String, String>()
        t["asd"] = "skdjafs"
        return serializer.stringify(t, String::class, String::class)
    }

    private fun createVcs(backupData: BackupData): Map<String, String> {
        val vcs = HashMap<String, String>()
        backupData.vcs.keys.forEach { vc ->
            vcs[vc.jti] = vc.raw
        }
        return vcs
    }

    private fun createVcsMetaData(backupData: BackupData): Map<String, Map<String, String>> {
        val vcsMetaData = HashMap<String, Map<String, String>>()
        backupData.vcs.forEach { entry ->
            vcsMetaData[entry.key.jti] = entry.value
        }
        return vcsMetaData
    }

    fun restoreBackup(backup: String): BackupData {

        TODO()
    }
}