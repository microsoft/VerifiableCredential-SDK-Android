// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.models.linkedDomains.DomainLinkageCredential
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtDomainLinkageCredentialValidator @Inject constructor(
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : DomainLinkageCredentialValidator {

    override suspend fun validate(domainLinkageCredential: String, rpDid: String, rpDomain: String): Boolean {
        val jwt = JwsToken.deserialize(domainLinkageCredential, serializer)
        val domainLinkageCredentialParsed = serializer.decodeFromString(DomainLinkageCredential.serializer(), jwt.content())
        if (!(jwtValidator.verifySignature(jwt) && jwtValidator.validateDidInHeaderAndPayload(jwt, domainLinkageCredentialParsed.issuer)))
            return false
        return verifyDidConfigResource(domainLinkageCredentialParsed, rpDid, rpDomain)
    }

    private fun verifyDidConfigResource(domainLinkageCredential: DomainLinkageCredential, rpDid: String, rpDomain: String): Boolean {
        return isCredentialSubjectIdValid(domainLinkageCredential, rpDid)
            && isCredentialSubjectOriginValid(domainLinkageCredential, rpDomain)
    }

    private fun isCredentialSubjectIdValid(domainLinkageCredential: DomainLinkageCredential, rpDid: String): Boolean {
        return domainLinkageCredential.subject == domainLinkageCredential.vc.credentialSubject.did
            && domainLinkageCredential.issuer == domainLinkageCredential.vc.credentialSubject.did
            && domainLinkageCredential.vc.credentialSubject.did == rpDid
    }

    private fun isCredentialSubjectOriginValid(domainLinkageCredential: DomainLinkageCredential, rpDomain: String): Boolean {
        return domainLinkageCredential.vc.credentialSubject.domainUrl.equals(rpDomain, true)
    }
}