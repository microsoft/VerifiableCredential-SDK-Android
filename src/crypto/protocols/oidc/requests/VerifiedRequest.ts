/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import PublicKey from '../../../keys/PublicKey';

/**
 * Verified and decrypted JOSE request
 * @interface
 */
export default interface VerifiedRequest {
  /** 
   * Fully qualified key id of the local key 
   */
  readonly localKeyId: string;
  /** 
   * Requesters PublicKey 
   */
  readonly requesterPublicKey: PublicKey;
  /** 
   * Request Nonce 
   */
  readonly nonce: string;
  /** 
   * Plaintext of the request
   */
  request: string;
}
