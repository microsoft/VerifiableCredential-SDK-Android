import { CryptoAlgorithm } from "./keyStore/IKeyStore";

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Base error class for the crypto.
 */
export default class CryptoError extends Error {
  /**
   * The algorithm name having the error.
   */
  public algorithm: CryptoAlgorithm;

  /**
   * Create instance of @class CryptoProtocolError
   * @param protocol name
   * @param message for the error
   */
  constructor (algorithm: CryptoAlgorithm, message: string) {
    super(message);
    // NOTE: Extending 'Error' breaks prototype chain since TypeScript 2.1.
    // The following line restores prototype chain.
    Object.setPrototypeOf(this, new.target.prototype);
    this.algorithm = algorithm;
  }
}
