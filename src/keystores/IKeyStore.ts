/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { DidKey } from '@decentralized-identity/did-crypto-typescript';
import { SignatureFormat } from './SignatureFormat';

/**
 * Interface defining methods and properties to
 * be implemented by specific key stores.
 */
export default interface IKeyStore {
  /**
   * Returns the key associated with the specified
   * key identifier.
   * @param keyIdentifier for which to return the key.
   */
  getKey (keyIdentifier: string): Promise<Buffer | DidKey>;

  /**
   * Saves the specified key to the key store using
   * the key identifier.
   * @param keyIdentifier for the key being saved.
   * @param key being saved to the key store.
   */
  save (keyIdentifier: string, key: Buffer | DidKey): Promise<void>;

  /**
   * Sign the data with the key referenced by keyIdentifier.
   * @param keyIdentifier for the key used for signature.
   * @param data Data to sign
   * @param format Signature format
   */
  sign (keyIdentifier: string, data: string, format: SignatureFormat): Promise<string>;
}
