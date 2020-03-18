package com.microsoft.portableIdentity.sdk.auth.models.siop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestClaimParameter (
    val userInfo: UserInfoRequest? = null,
    @SerialName("id_token")
    val idToken: Map<String, MemberScope<String>>? = null
) {
    @Serializable
    data class UserInfoRequest(
        val name: MemberScope<String>? = MemberScope.default(),
        @SerialName("given_name")
        val givenName: MemberScope<String>? = MemberScope.default(),
        @SerialName("family_name")
        val familyName: MemberScope<String>? = MemberScope.default(),
        @SerialName("middle_name")
        val middleName: MemberScope<String>? = MemberScope.default(),
        val nickname: MemberScope<String>? = MemberScope.default(),
        @SerialName("preferred_username")
        val preferredUsername: MemberScope<String>? = MemberScope.default(),
        val profile: MemberScope<String>? = MemberScope.default(),
        val picture: MemberScope<String>? = MemberScope.default(),
        val website: MemberScope<String>? = MemberScope.default(),
        val email: MemberScope<String>? = MemberScope.default(),
        @SerialName("email_verified")
        val emailVerified: MemberScope<Boolean>? = MemberScope.default(),
        val gender: MemberScope<String>? = MemberScope.default(),
        val birthdate: MemberScope<String>? = MemberScope.default(),
        val zoneinfo: MemberScope<String>? = MemberScope.default(),
        val locale: MemberScope<String>? = MemberScope.default(),
        @SerialName("phone_number")
        val phoneNumber: MemberScope<String>? = MemberScope.default(),
        @SerialName("phone_number_verified")
        val phoneNumberVerified: MemberScope<Boolean>? = MemberScope.default(),
        val address: MemberScope<Address>? = MemberScope.default(),
        @SerialName("updated_at")
        val updatedAt: MemberScope<Int>? = MemberScope.default()
    ) {
        @Serializable
        data class Address(
            val formatted: String?,
            @SerialName("street_address")
            val streetAddress: String?,
            val locality: String?,
            val region: String?,
            @SerialName("postal_code")
            val postalCode: String?,
            val country: String?
        )
    }

    @Serializable
    data class MemberScope<T>(
        val essential: Boolean? = false,
        val value: T? = null,
        val values: List<T>? = null,
        val undefined: Boolean = false
    ) {
        companion object {
            fun <T>default(): MemberScope<T> {
                return MemberScope(undefined = true)
            }
        }
    }

    fun getRequestedClaimClasses(): Map<String, Boolean> {
        return if (idToken == null) {
            emptyMap()
        } else {
            val idTokenClaims = listOf("iss",
                "sub",
                "aud",
                "exp",
                "iat",
                "auth_time",
                "nonce",
                "acr",
                "amr",
                "azp")
            val requestedClasses = mutableMapOf<String, Boolean>()
            idToken.filter {
                !(it.key in idTokenClaims)
            }.forEach {
                requestedClasses[it.key] = it.value.essential ?: false
            }
            requestedClasses
        }
    }
}