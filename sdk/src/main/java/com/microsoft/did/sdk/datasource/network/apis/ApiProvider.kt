/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.apis

import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiProvider @Inject constructor(retrofit: Retrofit) {

    // maybe refactor these into different interface apis?
    val presentationApis: PresentationApis = retrofit.create(PresentationApis::class.java)

    val issuanceApis: IssuanceApis = retrofit.create(IssuanceApis::class.java)

    val revocationApis: RevocationApis = retrofit.create(RevocationApis::class.java)

    val linkedDomainsApis: LinkedDomainsApis = retrofit.create(LinkedDomainsApis::class.java)

    val identifierApi: IdentifierApi = retrofit.create(IdentifierApi::class.java)
}