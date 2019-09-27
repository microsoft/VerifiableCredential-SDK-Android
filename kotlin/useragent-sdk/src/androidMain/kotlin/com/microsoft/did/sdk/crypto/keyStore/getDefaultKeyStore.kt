package com.microsoft.did.sdk.crypto.keyStore

actual fun getDefaultKeyStore(): IKeyStore {
    return AndroidKeyStore()
}