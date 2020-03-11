package com.microsoft.portableIdentity.sdk.auth.credentialRequests

/**
 * Object that represents a Self-Issued Claim.
 */
data class SelfIssuedClaimRequest(

    override val id: String,

    /**
     * The type the claim should be.
     */
    override val type: CredentialRequestType = CredentialRequestType.SelfIssued

): CredentialRequest