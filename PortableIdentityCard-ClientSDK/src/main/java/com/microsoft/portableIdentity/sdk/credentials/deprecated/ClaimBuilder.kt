package com.microsoft.portableIdentity.sdk.credentials.deprecated

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer

class ClaimBuilder(forClass: ClaimClass? = null) {
    var context: String? = null
    var type: String? = null
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
    private val claimDetails: MutableList<Map<String, String>> = mutableListOf()


    fun addClassDescription(header: String, body: String) {
        claimClassDescriptions.add(
            ClaimDescription(
                header,
                body
            )
        )
    }

    fun addClaimDescription(header: String, body: String) {
        claimDescriptions.add(ClaimDescription(header, body))
    }

    fun addClaimDetail(claim: Map<String, String>) {
        claimDetails.add(claim)
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

    fun buildObject(classUri: String, identifier: Identifier, cryptoOperations: CryptoOperations? = null): ClaimObject {
        if (context.isNullOrBlank() || type.isNullOrBlank()) {
            throw SdkLog.error("Context and Type must be set.")
        }
        val claims = if (cryptoOperations != null) {
            val serializedData = Serializer.stringify(claimDetails, Map::class)
            val token = JwsToken(serializedData)
            token.sign(identifier.signatureKeyReference, cryptoOperations)
            SignedClaimDetail(
                data = token.serialize(JwsFormat.Compact)
            )
        } else {
            UnsignedClaimDetail(
                data = claimDetails.toList()
            )
        }
        return ClaimObject(
            classUri,
            context!!,
            type!!,
            identifier.document.id,
            claimDescriptions,
            claims
        )
    }
}