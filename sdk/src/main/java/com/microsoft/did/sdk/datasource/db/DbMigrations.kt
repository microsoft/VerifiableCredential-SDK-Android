// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DbMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE VerifiableCredentialHolder ADD COLUMN isArchived BOOLEAN NOT NULL DEFAULT(false)")
            database.execSQL("DROP TABLE VerifiableCredential")
        }
    }
}