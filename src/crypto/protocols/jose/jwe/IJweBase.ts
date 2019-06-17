/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IJweRecipient from './IJweRecipient';
import { ProtectionFormat } from '../../../keyStore/ProtectionFormat';
import { TSMap } from 'typescript-map'

/**
 * Defines a header in JWE
 */
export type JweHeader = TSMap<string, string>;

/**
 * JWE general json format
 */
export default interface IJweBase {

  /**
   * The protected header.
   */
  protected: JweHeader,

  /**
   * The unprotected header.
   */
  unprotected: JweHeader,

  /**
   * The initial vector.
   */
  iv: Buffer,

  /**
   * The additional authenticated data.
   */
  aad: Buffer,

  /**
   * The encrypted data.
   */
  ciphertext: Buffer,

  /**
   * The authentication tag used by GCM.
   */
  tag: Buffer,

  /**
   * The serialization format
   */
  format: ProtectionFormat
}
