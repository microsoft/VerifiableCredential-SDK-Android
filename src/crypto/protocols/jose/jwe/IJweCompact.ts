
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JweHeader } from "./IJweGeneralJson";

/**
 * JWS compact format
 */
export default interface IJwsCompact  {

  /**
   * The protected (signed) header.
   */
  protected?: JweHeader | undefined,

  /**
   * The application-specific non-encoded payload.
   */
  payload: string,

  /**
   * The signature
   */
  signature: string
}
