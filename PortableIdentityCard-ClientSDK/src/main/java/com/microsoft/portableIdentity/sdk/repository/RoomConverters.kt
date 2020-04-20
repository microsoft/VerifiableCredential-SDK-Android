/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.room.TypeConverter
import com.microsoft.portableIdentity.sdk.auth.models.contracts.display.DisplayContract
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import javax.inject.Inject

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
}