package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.CryptoOperations

expect fun getDefaultKeyStore(parent: CryptoOperations): IKeyStore