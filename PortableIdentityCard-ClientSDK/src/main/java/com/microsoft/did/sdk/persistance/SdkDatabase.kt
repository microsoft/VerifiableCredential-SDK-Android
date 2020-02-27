// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.persistance

import androidx.room.Database
import androidx.room.RoomDatabase
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.credentials.SerialClaimObject
import com.microsoft.did.sdk.persistance.dao.ClaimObjectDao
import com.microsoft.did.sdk.persistance.dao.SerialClaimObjectDao

@Database(entities = [ClaimObject::class, SerialClaimObject::class], version = 1)
abstract class SdkDatabase : RoomDatabase() {
    abstract fun claimObjectDao(): ClaimObjectDao

    abstract fun serialClaimObjectDao(): SerialClaimObjectDao
}