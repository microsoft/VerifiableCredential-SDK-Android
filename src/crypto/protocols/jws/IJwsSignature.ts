
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JwsHeader } from "./IJwsGeneralJson";

/**
 * JWS signature used by the general JSON
 */
export default interface IJwsSignature {
  /**
   * The protected (signed) header.
   */
  protected?: JwsHeader | undefined,

  /**
   * The unprotected (unverified) header.
   */
  header?: JwsHeader | undefined,

  /**
   * The JWS signature.
   */
  signature: Buffer
}
