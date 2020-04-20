package com.microsoft.portableIdentity.sdk.auth.requests

import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestation
import com.microsoft.portableIdentity.sdk.auth.models.attestations.PresentationAttestationToCardsBindings
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import javax.inject.Singleton

@Singleton
class CardConverter {

    fun getRequiredSavedCards(presentationAttestations: List<PresentationAttestation>, cards: List<PortableIdentityCard>): Result<PresentationAttestationToCardsBindings> {
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
        return Result.Success(PresentationAttestationToCardsBindings(typeToSavedCards, neededCards))
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