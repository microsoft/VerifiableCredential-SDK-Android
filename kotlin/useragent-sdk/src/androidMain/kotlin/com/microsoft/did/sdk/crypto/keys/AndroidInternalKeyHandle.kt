package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import java.security.KeyStore

class AndroidInternalKeyHandle(val key: KeyStore.PrivateKeyEntry): AndroidCryptoKeyHandle(KeyType.Private)