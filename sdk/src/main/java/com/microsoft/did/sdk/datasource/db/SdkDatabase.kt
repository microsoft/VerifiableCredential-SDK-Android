// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.microsoft.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.did.sdk.identifier.models.Identifier

/**
 * Abstract description of the database interface that is supposed to be provided by Room. New entities have to be
 * added here, so that the schema includes them.
 *
 * Whenever the schema is changed the version number has to be increased by one and a migration strategy needs to be
 * provided, otherwise the database will be deleted and recreated.
 *
 * More info:
 * https://developer.android.com/topic/libraries/architecture/room
 */
@Database(entities = [Identifier::class], version = 1)
abstract class SdkDatabase : RoomDatabase() {

    abstract fun identifierDao(): IdentifierDao
}