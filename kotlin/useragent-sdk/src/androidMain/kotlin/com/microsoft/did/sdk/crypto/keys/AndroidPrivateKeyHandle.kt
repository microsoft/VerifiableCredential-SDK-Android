package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import java.security.PrivateKey

class AndroidPrivateKeyHandle(val key: PrivateKey): AndroidCryptoKeyHandle(KeyType.Private)