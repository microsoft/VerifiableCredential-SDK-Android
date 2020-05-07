package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential

class PairwiseIssuanceRequest(override val audience: String, val verifiableCredential: VerifiableCredential) : ServiceRequest {
}