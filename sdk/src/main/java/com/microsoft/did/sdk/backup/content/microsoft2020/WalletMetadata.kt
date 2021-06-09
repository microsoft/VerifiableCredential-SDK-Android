package com.microsoft.did.sdk.backup.content.microsoft2020

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("BaseWallet")
open class WalletMetadata {
    var seed: String = ""
}