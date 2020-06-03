package com.microsoft.did.sdk.identifier.models.identifierdocument

enum class LinkedDataKeySpecification(val values: List<String>) {
    Ed25519Signature2018(listOf("Ed25519VerificationKey2018", "Ed25519Signature2018")),
    RsaSignature2018(listOf("RsaVerificationKey2018", "RsaSignature2018")),
    EcdsaKoblitzSignature2016(listOf("EcdsaKoblitzSignature2016")),
    EcdsaSecp256k1Signature2019(listOf("EcdsaSecp256k1VerificationKey2019", "Secp256k1VerificationKey2018", "EcdsaSecp256k1Signature2019"))
}