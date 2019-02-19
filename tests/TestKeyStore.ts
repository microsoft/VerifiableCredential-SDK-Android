/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import KeyStore from '../src/keystores/KeyStore';

/**
 * Implementation of a resolver for testing.
 * @class
 * @implmenets Resolver
 */
export default class TestKeyStore implements KeyStore {
  
  /**
   * Prepares the resolver for the test run.
   * @param identifier to use for the test.
   * @param identifierDocument to use for the test.
   */
  public prepareTest() {
  }

  /**
   * Returns the key associated with the specified
   * key identifier.
   * @param keyIdentifier for which to return the key.
   */
  public async get(keyIdentifier: string): Promise<Buffer> {
    if (keyIdentifier) {
      return new Buffer('');
    }

    return new Buffer('');
  }

  /**
   * Saves the specified key to the key store using
   * the key identifier.
   * @param keyIdentifier for the key being saved.
   * @param key being saved to the key store.
   */
  public async save(keyIdentifier: string, key: Buffer): Promise<boolean> {
    if (keyIdentifier && key) {
      return true;
    }
    
    return false;
  }
}
