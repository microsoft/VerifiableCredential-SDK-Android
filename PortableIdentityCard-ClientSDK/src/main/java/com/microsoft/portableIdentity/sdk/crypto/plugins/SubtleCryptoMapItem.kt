package com.microsoft.portableIdentity.sdk.crypto.plugins

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto

data class SubtleCryptoMapItem (val subtleCrypto: SubtleCrypto, val scope: SubtleCryptoScope)