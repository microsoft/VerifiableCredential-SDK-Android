package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.CryptoOperations

actual fun getDefaultKeyStore(parent: CryptoOperations): IKeyStore {
    return AndroidKeyStore(parent)
}