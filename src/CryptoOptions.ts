/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from './crypto/plugin/CryptoFactory';
import SubtleCryptoOperations from './crypto/plugin/SubtleCryptoOperations';
import KeyStoreInMemory from './crypto/keyStore/KeyStoreInMemory';

/**
 * Class used to model crypto options
 */
export default class CryptoOptions {
  /**
   * Get or set the crypto api to be used. Initialize the default crypto plugin.
   */
  public cryptoFactory: CryptoFactory = new CryptoFactory(new KeyStoreInMemory(), new SubtleCryptoOperations());

  /**
   * Get or set the algorithm to be used.
   * Conform to the Web Cryptography Api
   */
  public algorithm: any;
}
