// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.models.dnsBinding.DomainLinkageCredential
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.util.serializer.Serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtDomainLinkageCredentialValidator @Inject constructor(
    private val jwtValidator: JwtValidator,
    private val serializer: Serializer
) : DomainLinkageCredentialValidator {

    override suspend fun validate(domainLinkageCredential: String, rpDid: String, rpDomain: String): Boolean {
        val jwt = JwsToken.deserialize(domainLinkageCredential, serializer)
        val domainLinkageCredential = serializer.parse(DomainLinkageCredential.serializer(), jwt.content())
        if (!jwtValidator.verifySignature(jwt)) {
            return false
        }
        return verifyDidConfigResource(domainLinkageCredential, rpDid, rpDomain)
    }

    //TODO: validate expiration date once it is in
    private fun verifyDidConfigResource(domainLinkageCredential: DomainLinkageCredential, rpDid: String, rpDomain: String): Boolean {
        return isCredentialSubjectIdValid(domainLinkageCredential, rpDid)
            && isCredentialSubjectOriginValid(domainLinkageCredential, rpDomain)
            && domainLinkageCredential.vc.issuanceDate.isNotEmpty()
    }

    private fun isCredentialSubjectIdValid(domainLinkageCredential: DomainLinkageCredential, rpDid: String): Boolean {
        return (domainLinkageCredential.subject == domainLinkageCredential.vc.credentialSubject.did)
            && (domainLinkageCredential.issuer == domainLinkageCredential.vc.credentialSubject.did)
            && domainLinkageCredential.vc.credentialSubject.did == rpDid
    }

    private fun isCredentialSubjectOriginValid(domainLinkageCredential: DomainLinkageCredential, rpDomain: String): Boolean {
        return domainLinkageCredential.vc.credentialSubject.domainUrl.isNotEmpty() && domainLinkageCredential.vc.credentialSubject.domainUrl == rpDomain
    }
}