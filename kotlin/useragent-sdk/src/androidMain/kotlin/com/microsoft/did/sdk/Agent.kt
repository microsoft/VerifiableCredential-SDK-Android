package com.microsoft.did.sdk

import AndroidSubtle
import android.content.Context
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore

class Agent(context: Context,
            registrationUrl: String = defaultRegistrationUrl,
            resolverUrl: String = defaultResolverUrl): AbstractAgent(registrationUrl, resolverUrl, subtleCrypto = AndroidSubtle(), keyStore = AndroidKeyStore(context))
