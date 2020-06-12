// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.microsoft.did.sdk.credential.models.PortableIdentityCard
import com.microsoft.did.sdk.credential.receipts.Receipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.datasource.db.dao.PortableIdentityCardDao
import com.microsoft.did.sdk.datasource.db.dao.IdentifierDao
import com.microsoft.did.sdk.datasource.db.dao.ReceiptDao
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialDao

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
@Database(entities = [VerifiableCredential::class, PortableIdentityCard::class, Identifier::class, Receipt::class], version = 2)
@TypeConverters(RoomConverters::class)
abstract class SdkDatabase : RoomDatabase() {

    abstract fun cardDao(): PortableIdentityCardDao

    abstract fun receiptDao(): ReceiptDao

    abstract fun verifiableCredentialDao(): VerifiableCredentialDao

    abstract fun identifierDao(): IdentifierDao
}