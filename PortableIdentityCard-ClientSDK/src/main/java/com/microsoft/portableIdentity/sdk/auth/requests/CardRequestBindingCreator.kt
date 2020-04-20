package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CardRequestBinding
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import javax.inject.Singleton

/**
 * TODO(better naming)
 * This class purpose it to take in a List of Presentation Attestations (Verifiable Credential Requests)
 * And a list of Portable Identity Cards and produce:
 * 1. Mapping of Credential Types to Cards that are of that Type.
 * 2. A List of all Presentation Attestations that have no cards that match the required Credential Type.
 */
@Singleton
class CardRequestBindingCreator {

    fun getRequiredSavedCards(presentationAttestations: List<PresentationAttestation>, cards: List<PortableIdentityCard>): CardRequestBinding {
        var typeToSavedCards = mutableMapOf<String, List<PortableIdentityCard>>()
        var neededCards = mutableListOf<PresentationAttestation>()
        presentationAttestations.forEach {
            val filteredCards = filterCardsByType(it.credentialType, cards)
            if (filteredCards.isEmpty()) {
                neededCards.add(it)
            } else {
                typeToSavedCards[it.credentialType] = filteredCards
            }
        }
        return CardRequestBinding(typeToSavedCards, neededCards)
    }

    private fun filterCardsByType(type: String, cards: List<PortableIdentityCard>): List<PortableIdentityCard> {
        val filteredCards = mutableListOf<PortableIdentityCard>()
        cards.forEach {
            if (it.verifiableCredential.type.contains(type)) {
                filteredCards.add(it)
            }
        }
        return filteredCards
    }
}