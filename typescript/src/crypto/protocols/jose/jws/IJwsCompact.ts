
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JwsHeader } from "./IJwsGeneralJson";

/**
 * JWS compact format
 */
export default interface IJwsCompact  {

  /**
   * The protected (signed) header.
   */
  protected?: JwsHeader | undefined,

  /**
   * The application-specific non-encoded payload.
   */
  payload: Buffer,

  /**
   * The signature
   */
  signature: string
}
