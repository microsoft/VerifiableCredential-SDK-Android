/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import nodeWebcryptoOssl from 'node-webcrypto-ossl';
const crypto = new nodeWebcryptoOssl();

/**
 * Class used to model crypto options
 */
export default class CryptoOptions {
  /**
   * Get or set the crypto api to be used
   */
  public cryptoApi: any = crypto;

  /**
   * Get or set the algorithm to be used.
   * Conform to the Web Cryptography Api
   */
  public algorithm: any;
}
