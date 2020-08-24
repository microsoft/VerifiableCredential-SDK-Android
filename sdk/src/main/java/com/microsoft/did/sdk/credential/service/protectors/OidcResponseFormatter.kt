/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import javax.inject.Singleton

/**
 * Class that forms Response Contents Properly.
 */
@Singleton
interface OidcResponseFormatter

typealias RequestedVcIdToVchMap = Map<Pair<String, Int>, VerifiableCredentialHolder>