/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { DidKey } from '@decentralized-identity/did-common-typescript';
import IKeyStore from '../../src/keystores/IKeyStore';

/**
 * Class defining methods and properties to mock a KeyStore
 */
export default class KeyStoreMock implements IKeyStore {
  private store: Map<string, Buffer | DidKey> = new Map<string, Buffer | DidKey>();

  /**
   * Returns the key associated with the specified
   * key identifier.
   * @param keyIdentifier for which to return the key.
   */
  get (keyIdentifier: string): Promise<Buffer | DidKey> {
    console.log(this.store.toString() + keyIdentifier);
    return new Promise((resolve, reject) => {
      if (this.store.has(keyIdentifier)) {
        resolve(this.store.get(keyIdentifier));
      } else {
        reject(`${keyIdentifier} not found`);
      }
    });
  }

  /**
   * Saves the specified key to the key store using
   * the key identifier.
   * @param keyIdentifier for the key being saved.
   * @param key being saved to the key store.
   */
  save (keyIdentifier: string, key: Buffer | DidKey): Promise<void> {
    console.log(this.store.toString() + keyIdentifier + key.toString());
    this.store.set(keyIdentifier, key);
    return new Promise((resolve) => {
      resolve();
    });
  }
}
