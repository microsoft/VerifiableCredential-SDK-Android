package com.microsoft.portableIdentity.sdk.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.credentials.deprecated.SerialClaimObject
import com.microsoft.portableIdentity.sdk.repository.dao.ClaimObjectDao
import com.microsoft.portableIdentity.sdk.repository.dao.SerialClaimObjectDao

@Database(entities = [ClaimObject::class, SerialClaimObject::class], version = 1)
abstract class SdkDatabase : RoomDatabase() {
    abstract fun claimObjectDao(): ClaimObjectDao

    abstract fun serialClaimObjectDao(): SerialClaimObjectDao
}