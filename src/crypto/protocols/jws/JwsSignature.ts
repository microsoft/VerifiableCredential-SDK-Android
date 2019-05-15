
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JwsHeader } from "./IJwsGeneralJson";
import IJwsSignature from "./IJwsSignature";

/**
 * JWS signature used by the general JSON
 */
export default class JwsSignature implements IJwsSignature {
  /**
   * The protected (signed) header.
   */
  public protected?: JwsHeader;

  /**
   * The unprotected (unverified) header.
   */
  public header?: JwsHeader;

  /**
   * The JWS signature.
   */
  public signature: Buffer;

  constructor() {
    this.signature = Buffer.from('');
  }
}