
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JweHeader } from "./IJweGeneralJson";
import IJweRecipient from "./IJweRecipient";

/**
 * JWS signature used by the general JSON
 */
export default class JweRecipient implements IJweRecipient {
 
  /**
   * The unprotected (unverified) header.
   */
  public header?: JweHeader;

  /**
   * The JWE signature.
   */
  public encrypted_key: Buffer


  constructor() {
    this.encrypted_key = Buffer.from('');
  }
}