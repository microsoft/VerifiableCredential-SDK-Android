/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PublicKey from '../../../keys/PublicKey';

/**
 * Verified and decrypted JOSE response
 * @interface
 */
export default interface VerifiedResponse {
  /** 
   * Fully qualified key id of the local key
   */
  readonly localKeyId: string;
  /** 
   * Responders PublicKey 
   */
  readonly responderPublicKey: PublicKey;
  /** 
   * Response Nonce 
   */
  readonly nonce: string;
  /** 
   * Plaintext of the response 
   */
  response: string;
}
