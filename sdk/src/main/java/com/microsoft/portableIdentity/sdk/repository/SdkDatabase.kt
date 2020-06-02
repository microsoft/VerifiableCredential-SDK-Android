/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.dao.PortableIdentityCardDao
import com.microsoft.portableIdentity.sdk.repository.dao.IdentifierDao
import com.microsoft.portableIdentity.sdk.repository.dao.ReceiptDao
import com.microsoft.portableIdentity.sdk.repository.dao.VerifiableCredentialDao

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
@Database(entities = [VerifiableCredential::class, PortableIdentityCard::class, Identifier::class, Receipt::class], version = 1)
@TypeConverters(RoomConverters::class)
abstract class SdkDatabase : RoomDatabase() {

    abstract fun cardDao(): PortableIdentityCardDao

    abstract fun receiptDao(): ReceiptDao

    abstract fun verifiableCredentialDao(): VerifiableCredentialDao

    abstract fun identifierDao(): IdentifierDao
}