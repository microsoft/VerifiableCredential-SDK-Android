/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * JWS signature used by the general JSON
 */
export default interface IJwsSignature {
  /**
   * The protected (signed) header.
   */
  protected?: {[name: string]: string} | undefined,

  /**
   * The unprotected (unverified) header.
   */
  header?: {[name: string]: string} | undefined,

  /**
   * The JWS signature.
   */
  signature: Buffer
}