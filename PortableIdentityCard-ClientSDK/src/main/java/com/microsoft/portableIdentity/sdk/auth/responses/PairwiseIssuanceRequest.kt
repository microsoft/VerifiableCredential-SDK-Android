package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.utilities.controlflow.PairwiseIssuanceException

class PairwiseIssuanceRequest(val verifiableCredential: VerifiableCredential) : ServiceRequest {
    override val audience: String = "https://exchangeServiceEndpoint.com"//verifiableCredential.contents.vc.exchangeService?.id ?: throw PairwiseIssuanceException("No Exchange Service in Verifiable Credential.")
}