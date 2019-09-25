package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import java.security.PublicKey

class AndroidPublicKeyHandle(val key: PublicKey): AndroidCryptoKeyHandle(KeyType.Public)