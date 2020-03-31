/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.credentials.deprecated.SerialClaimObject
import com.microsoft.portableIdentity.sdk.repository.dao.ClaimObjectDao
import com.microsoft.portableIdentity.sdk.repository.dao.SerialClaimObjectDao

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
@Database(entities = [ClaimObject::class, SerialClaimObject::class], version = 1)
abstract class SdkDatabase : RoomDatabase() {
    abstract fun claimObjectDao(): ClaimObjectDao

    abstract fun serialClaimObjectDao(): SerialClaimObjectDao
}