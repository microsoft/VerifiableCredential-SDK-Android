/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PrivateKey from '../keys/PrivateKey';
import PublicKey from '../keys/PublicKey';
import CryptoFactory from '../plugin/CryptoFactory';
import { ProtectionFormat } from './ProtectionFormat';


/**
 * Interface defining IKeyStore options.
 */
export interface IKeyStoreOptions {
  // The crypto algorithm suites used for signing
  cryptoFactory: CryptoFactory,

  // The key store
  keyStore: IKeyStore,

  // The used algorithm
  algorithm?: Algorithm,

  // The default protected header
  protected?: { [name: string]: string },

  // The default header
  header?: { [name: string]: string },

  // Make the type indexable
  [key: string]: any;
}

/**
 * Interface defining signature options.
 */
export interface ISigningOptions extends IKeyStoreOptions {
}

/**
 * Interface defining encryption options.
 */
export interface IEncryptionOptions extends IKeyStoreOptions {
  // The key encryption algorithm
  kekAlgorithm: Algorithm
}

/**
 * Interface defining methods and properties to
 * be implemented by specific key stores.
 */
export default interface IKeyStore {
  /**
   * Returns the key associated with the specified
   * key reference.
   * @param keyIdentifier for which to return the key.
   * @param publicKeyOnly True if only the public key is needed.
   */
  get (keyReference: string, publicKeyOnly: boolean): Promise<Buffer | PrivateKey | PublicKey>;

  /**
   * Saves the specified key to the key store using
   * the key reference.
   * @param keyReference Reference for the key being saved.
   * @param key being saved to the key store.
   */
  save (keyReference: string, key: Buffer | PrivateKey | PublicKey): Promise<void>;

  /**
   * Lists all key references with their corresponding key ids
   */
  list (): Promise<{ [name: string]: string }>;

  /**
   * Sign the data with the key referenced by keyReference.
   * @param keyReference Reference to the key used for signature.
   * @param data Data to sign
   * @param format used to protect the content
   * @param signingOptions Set of signing options
   * @returns The protected message
   */
  sign (keyReference: string, data: string | Buffer, format: ProtectionFormat, signingOptions: ISigningOptions): Promise<any>;

  /**
   * Decrypt the data with the key referenced by keyReference.
   * @param keyReference Reference to the key used for signature.
   * @param cipher Data to decrypt
   * @param format Protection format used to decrypt the data
   * @param encryptionOptions Set of encryption options
   * @returns The plain text message
   */
  decrypt (keyReference: string, cipher: string | Buffer, encryptionOptions: IEncryptionOptions): Promise<any>;
}
