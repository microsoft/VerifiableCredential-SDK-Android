/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * JWS compact format
 */
export default interface IJwsCompact  {

  /**
   * The protected (signed) header.
   */
  protected?: {[name: string]: string} | undefined,

  /**
   * The application-specific non-encoded payload.
   */
  payload: string,

  /**
   * The signature
   */
  signature: string
};
