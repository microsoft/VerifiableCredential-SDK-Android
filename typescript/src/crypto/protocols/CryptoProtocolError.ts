/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Base error class for the crypto protocols.
 */
export default class CryptoProtocolError extends Error {
  /**
   * The protocol name having the error.
   */
  public protocol: string;

  /**
   * Create instance of @class CryptoProtocolError
   * @param protocol name
   * @param message for the error
   */
  constructor (protocol: string, message: string) {
    super(message);
    // NOTE: Extending 'Error' breaks prototype chain since TypeScript 2.1.
    // The following line restores prototype chain.
    Object.setPrototypeOf(this, new.target.prototype);
    this.protocol = protocol;
  }
}
