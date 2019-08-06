package com.microsoft.useragentSdk.crypto.models

data class KeyData(
    val jwk: JsonWebKey? = null,
    val data: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as KeyData

        if (jwk != other.jwk) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = jwk?.hashCode() ?: 0
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}