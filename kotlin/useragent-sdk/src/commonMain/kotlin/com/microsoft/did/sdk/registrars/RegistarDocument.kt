package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.identifier.IdentifierDocumentPublicKey

@S
data class RegistarDocument(val id: String, val publicKeys: ArrayList<IdentifierDocumentPublicKey>) {
}