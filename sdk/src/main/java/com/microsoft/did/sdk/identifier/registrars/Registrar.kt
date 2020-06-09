/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.identifier.registrars

import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result

/**
 * @interface defining methods and properties
 * to be implemented by specific registration methods.
 */
abstract class Registrar {

    /**
     * @return Identifier that was created.
     * @throws Exception if unable to create an Identifier.
     */
    abstract suspend fun register(identifier: Identifier): Result<Identifier>
}