package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto

data class SubtleCryptoMapItem(val subtleCrypto: SubtleCrypto, val scope: SubtleCryptoScope)