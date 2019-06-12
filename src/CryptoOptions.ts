/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from './crypto/plugin/CryptoFactory';
import SubtleCryptoBrowserOperations from './crypto/plugin/SubtleCryptoBrowserOperations';
import KeyStoreInMemory from './crypto/keyStore/KeyStoreInMemory';

/**
 * Class used to model crypto options
 */
export default class CryptoOptions {
  /**
   * Get or set the crypto api to be used. Initialize the default crypto plugin.
   */
  public cryptoFactory: CryptoFactory = new CryptoFactory(new KeyStoreInMemory(), new SubtleCryptoBrowserOperations());

  /**
   * Get or set the authentication algorithm.
   * Conform to the JWA standard
   */
  public authenticationSigningJoseAlgorithm: string = 'ES256K';
}
