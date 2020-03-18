package com.microsoft.portableIdentity.sdk.auth.models.siop

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserInfo (
    val name: String? = null,
    @SerialName("given_name")
    val givenName: String? = null,
    @SerialName("family_name")
    val familyName: String? = null,
    @SerialName("middle_name")
    val middleName: String? = null,
    val nickname: String? = null,
    @SerialName("preferred_username")
    val preferredUsername: String? = null,
    val profile: String? = null,
    val picture: String? = null,
    val website: String? = null,
    val email: String? = null,
    @SerialName("email_verified")
    val emailVerified: Boolean? = null,
    val gender: String? = null,
    val birthdate: String? = null,
    val zoneinfo: String? = null,
    val locale: String? = null,
    @SerialName("phone_number")
    val phoneNumber: String? = null,
    @SerialName("phone_number_verified")
    val phoneNumberVerified: Boolean? = null,
    val address: Address? = null,
    @SerialName("updated_at")
    val updatedAt: Int? = null
) {
    @Serializable
    data class Address (
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