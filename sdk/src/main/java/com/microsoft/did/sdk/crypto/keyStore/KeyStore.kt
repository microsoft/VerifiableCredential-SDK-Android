package com.microsoft.did.sdk.crypto.keyStore

import java.security.Key

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Interface defining methods and properties to
 * be implemented by specific key stores.
 */
abstract class KeyStore {

    /**
     * Returns the key associated with the specified
     * key reference.
     * @param keyId for which to return the key.
     */
    abstract fun <T : Key> getKey(keyId: String): T

    /**
     * Saves the specified key to the key store using
     * the key reference.
     * @param keyId Reference for the key being saved.
     * @param key being saved to the key store.
     */
    abstract fun saveKey(key: Key, keyId: String)
}