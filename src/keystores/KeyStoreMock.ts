import { DidKey } from '@decentralized-identity/did-common-typescript';

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Class defining methods and properties to mock a KeyStore
 */
export default class KeyStoreMock {
  private store: Map<string, Buffer | DidKey> = new Map<string, Buffer | DidKey>();

  /**
   * Returns the key associated with the specified
   * key identifier.
   * @param keyIdentifier for which to return the key.
   */
  get (keyIdentifier: string): Promise<Buffer | DidKey> {
    console.log(this.store.toString() + keyIdentifier);
    return new Promise((resolve) => {
      resolve(this.store.get(keyIdentifier));
    });
  }

  /**
   * Saves the specified key to the key store using
   * the key identifier.
   * @param keyIdentifier for the key being saved.
   * @param key being saved to the key store.
   */
  save (keyIdentifier: string, key: Buffer | DidKey): Promise<boolean> {
    console.log(this.store.toString() + keyIdentifier + key.toString());
    if (keyIdentifier.startsWith('identifier to simulate storage failure')) {
      return new Promise((resolve) => {
        resolve(false);
      });
    }

    this.store.set(keyIdentifier, key);
    return new Promise((resolve) => {
      resolve(true);
    });
  }
}
