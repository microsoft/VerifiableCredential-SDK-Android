// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credentials

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SerialClaimObject(val serialClaimObject: String) {
    @PrimaryKey(autoGenerate = true) var uid:Int = 0
}