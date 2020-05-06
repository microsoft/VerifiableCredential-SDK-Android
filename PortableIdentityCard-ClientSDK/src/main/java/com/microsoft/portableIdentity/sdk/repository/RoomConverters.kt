/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.room.TypeConverter
import com.microsoft.portableIdentity.sdk.auth.models.contracts.display.DisplayContract
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiablePresentationContent
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.utilities.Serializer

object RoomConverters {

    private val serializer: Serializer = Serializer()

    @TypeConverter
    @JvmStatic
    fun displayContractToString(displayContract: DisplayContract) = serializer.stringify(DisplayContract.serializer(), displayContract)

    @TypeConverter
    @JvmStatic
    fun stringToDisplayContract(serializedContract: String) = serializer.parse(DisplayContract.serializer(), serializedContract)

    @TypeConverter
    @JvmStatic
    fun verifiableCredentialToString(vc: VerifiableCredential) = serializer.stringify(VerifiableCredential.serializer(), vc)

    @TypeConverter
    @JvmStatic
    fun stringToVerifiableCredential(serializedVc: String) = serializer.parse(VerifiableCredential.serializer(), serializedVc)

    @TypeConverter
    @JvmStatic
    fun stringToIdentifierDocument(serializedDoc: String) = serializer.parse(IdentifierDocument.serializer(), serializedDoc)

    @TypeConverter
    @JvmStatic
    fun identifierDocumentToString(idDocument: IdentifierDocument) = serializer.stringify(IdentifierDocument.serializer(), idDocument)

    @TypeConverter
    @JvmStatic
    fun toReceiptAction(value: String) = enumValueOf<ReceiptAction>(value)

    @TypeConverter
    @JvmStatic
    fun fromReceiptAction(value: ReceiptAction) = value.name

    @TypeConverter
    @JvmStatic
    fun toVerifiableCredentialContent(value: String) = serializer.parse(VerifiablePresentationContent.serializer(), value)

    @TypeConverter
    @JvmStatic
    fun fromVerifiableCredentialContent(value: VerifiableCredentialContent) = serializer.stringify(VerifiableCredentialContent.serializer(), value)
}