package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.utilities.MinimalJson
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.stringify

class ClaimBuilder(forClass: ClaimClass? = null) {
    var issuerName: String? = forClass?.issuerName
    var claimLogo: ClaimClass.ClaimLogo? = forClass?.claimLogo
    var claimName: String? = forClass?.claimName
    var hexBackgroundColor: String? = forClass?.hexBackgroundColor
    var hexFontColor: String? = forClass?.hexFontColor
    var moreInfo: String? = forClass?.moreInfo
    val helpLinks: MutableMap<String, String> = forClass?.helpLinks?.toMutableMap() ?: mutableMapOf()
    private val claimClassDescriptions: MutableList<ClaimDescription> = forClass?.claimDescriptions?.toMutableList() ?: mutableListOf()
    var readPermissionDescription: PermissionDescription? = forClass?.readPermissionDescription

    private val claimDescriptions: MutableList<ClaimDescription> = mutableListOf()
    private val claimDetails: MutableList<String> = mutableListOf()


    fun addClassDescription(header: String, body: String) {
        claimClassDescriptions.add(ClaimDescription(header, body))
    }

    fun addClaimDescription(header: String, body: String) {
        claimDescriptions.add(ClaimDescription(header, body))
    }

    @ImplicitReflectionSerializer
    fun addClaimDetail(claim: Map<String, String>) {
        claimDetails.add(MinimalJson.serializer.stringify(claim))
    }

    fun buildClass(): ClaimClass {
        return ClaimClass(
            issuerName,
            claimLogo,
            claimName,
            hexBackgroundColor,
            hexFontColor,
            moreInfo,
            helpLinks,
            claimClassDescriptions,
            readPermissionDescription
        )
    }

    @ImplicitReflectionSerializer
    fun buildObject(classUri: String, identifier: Identifier, cryptoOperations: CryptoOperations? = null): ClaimObject {
        val claims = if (cryptoOperations != null) {
            claimDetails.map{
                val token = JwsToken(it)
                token.sign(identifier.signatureKeyReference, cryptoOperations)
                ClaimDetail(
                    type = ClaimDetail.JWS,
                    data = token.serialize(JwsFormat.Compact)
                )
            }
        } else {
            claimDetails.map{
                ClaimDetail(
                    type = ClaimDetail.UNSIGNED,
                    data = it
                )
            }
        }
        return ClaimObject(
            classUri,
            claimDescriptions,
            identifier.document.id,
            claims
        )
    }
}