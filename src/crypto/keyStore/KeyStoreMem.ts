/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { ProtectionFormat } from './ProtectionFormat';
import Protect from './Protect';
import PrivateKey from '../keys/PrivateKey';
import PublicKey from '../keys/PublicKey';
import IKeyStore, { ISigningOptions, IEncryptionOptions } from './IKeyStore';
import CryptoFactory from '../CryptoFactory';

/**
 * Class defining methods and properties for a light KeyStore
 */
export default class KeyStoreMem implements IKeyStore {
  private store: Map<string, Buffer | PrivateKey | PublicKey> = new Map<string, Buffer | PrivateKey | PublicKey>();

  /**
   * Returns the key associated with the specified
   * key identifier.
   * @param keyReference for which to return the key.
   * @param publicKeyOnly True if only the public key is needed.
   */
  get (keyReference: string, publicKeyOnly: boolean): Promise<Buffer | PrivateKey | PublicKey> {
    return new Promise((resolve, reject) => {
      if (this.store.has(keyReference)) {
        const key: any = this.store.get(keyReference);
        if (publicKeyOnly) {
          switch (key.kty.toLowerCase()) {
            case 'ec':
            case 'rsa':
              return resolve(key.getPublicKey());
            default:
              throw new Error(`A secret does not has a public key`);
          }
        } else {
          resolve(key);
        }

      } else {
        reject(`${keyReference} not found`);
      }
    });
  }

 /**
  * Lists all keys with their corresponding key ids
  */
  list (): Promise<{ [name: string]: string }> {
    const dictionary: { [name: string]: string } = {};
    for (let [key, value] of this.store) {
      if ((value as any).kid) {
        dictionary[key] = (value as any).kid;
      }
    }
    return new Promise((resolve) => {
      resolve(dictionary);
    });
  }

  /**
   * Saves the specified key to the key store using
   * the key identifier.
   * @param keyIdentifier for the key being saved.
   * @param key being saved to the key store.
   */
  save (keyIdentifier: string, key: Buffer | PrivateKey | PublicKey): Promise<void> {
    console.log(this.store.toString() + keyIdentifier + key.toString());
    this.store.set(keyIdentifier, key);
    return new Promise((resolve) => {
      resolve();
    });
  }

  /**
   * Sign the data with the key referenced by keyIdentifier.
   * @param keyReference for the key used for signature.
   * @param payload Data to sign
   * @param format used to protect the content
   * @param signingOptions Set of signing options
   * @returns The protected message
   */
  public async sign (keyReference: string,
    payload: string | Buffer,
    format: ProtectionFormat,
    signingOptions: ISigningOptions): Promise<string> {
    return Protect.sign(keyReference, payload, format, signingOptions);
  }

  /**
   * Decrypt the data with the key referenced by keyReference.
   * @param keyReference Reference to the key used for signature.
   * @param cipher Data to decrypt
   * @param encryptionOptions Set of encryption options
   * @returns The plain text message
   */
  public async decrypt (keyReference: string, cipher: string | Buffer, encryptionOptions: IEncryptionOptions): Promise<string> {
    return Protect.decrypt(keyReference, cipher, encryptionOptions);
  }
}
