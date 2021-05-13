// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.backup.container.jwe

import com.microsoft.did.sdk.crypto.protocols.jose.jwe.JweToken
import com.microsoft.did.sdk.datasource.backup.content.ProtectedBackupData

class JwePasswordProtectedBackupData internal constructor(
    val jweToken: JweToken,
) : ProtectedBackupData()