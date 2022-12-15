// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.validators

import android.util.Base64
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.DigestAlgorithm
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import com.microsoft.did.sdk.util.controlflow.InvalidSdJwtException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SdJwtValidator @Inject constructor() {

    class Disclosure(val claimName: String, val claimValue: String)

    private val serializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun verifyDisclosures(contents: VerifiableCredentialContent, disclosures: List<String>?): Boolean {
        if (disclosures == null) return true
        if (contents.sd_digest_derivation_alg != "sha-256") throw AlgorithmException("Unknown digest algorithm. Only 'sha-256' is supported at this time")
        val sdHashes = getSdSet(contents)
        for (disclosure in disclosures) {
            val digest = CryptoOperations.digest(disclosure.toByteArray(), DigestAlgorithm.Sha256)
            val disclosureDigestB64 = Base64.encodeToString(digest, Constants.BASE64_URL_SAFE)
            if (!sdHashes.contains(disclosureDigestB64)) throw InvalidSdJwtException("Disclosure $disclosureDigestB64 could not be found in \"_sd\" array.")
        }
        return true
    }

    private fun getSdSet(contents: VerifiableCredentialContent): Set<String> {
        val sdSet = HashSet<String>()
        val sdClaim = contents.vc.credentialSubject["_sd"] ?: "[]"
        val jsonArray = serializer.decodeFromString(JsonArray.serializer(), sdClaim)
        jsonArray.forEach { jsonValue ->
            sdSet.add(jsonValue.jsonPrimitive.content)
        }
        return sdSet
    }

    fun decodeDisclosure(b64Disclosure: String): Disclosure {
        val disclosureString = Base64.decode(b64Disclosure, Constants.BASE64_URL_SAFE).decodeToString()
        val jsonArray = serializer.decodeFromString(JsonArray.serializer(), disclosureString)
        return Disclosure(jsonArray[1].jsonPrimitive.content, jsonArray[2].jsonPrimitive.content)
    }
}