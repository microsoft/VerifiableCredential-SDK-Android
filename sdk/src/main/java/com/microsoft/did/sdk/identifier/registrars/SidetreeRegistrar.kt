/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.identifier.registrars

import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.RegistrarException
import com.microsoft.did.sdk.util.controlflow.Result
import javax.inject.Inject
import javax.inject.Named

/**
 * Registrar implementation for the Sidetree long form identifier
 * @param baseUrl url used for registering an identifier
 * @class
 * @implements Registrar
 */
class SidetreeRegistrar @Inject constructor(
    @Named("registrationUrl") private val baseUrl: String
) : Registrar() {

    /**
     * Registers Identifier that is passed in.
     * TODO(we do not support registration for MVP)
     */
    override suspend fun register(identifier: Identifier): Result<Identifier> {
        return try {
            Result.Success(identifier)
        } catch (exception: Exception) {
            Result.Failure(RegistrarException("Unable to create an identifier", exception))
        }
    }
}