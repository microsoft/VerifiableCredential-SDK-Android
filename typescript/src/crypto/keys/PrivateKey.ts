/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 import PublicKey from './PublicKey';
import JsonWebKey from './JsonWebKey';

/**
 * Represents a Private Key in JWK format.
 * @class
 * @abstract
 * @hideconstructor
 */
export default abstract class PrivateKey extends JsonWebKey {

  /**
   * Default Sign Algorithm for JWK 'alg' field
   */
  readonly alg: string = 'none';

  /**
   * Gets the corresponding public key
   * @returns The corresponding {@link PublicKey}
   */
  abstract getPublicKey (): PublicKey;
}
