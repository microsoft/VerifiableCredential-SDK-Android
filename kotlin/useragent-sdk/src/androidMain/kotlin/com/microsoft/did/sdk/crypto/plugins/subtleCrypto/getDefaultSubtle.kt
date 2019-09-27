package com.microsoft.did.sdk.crypto.plugins.subtleCrypto

import AndroidSubtle
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto

actual fun getDefaultSubtle(): SubtleCrypto {
    return AndroidSubtle()
}