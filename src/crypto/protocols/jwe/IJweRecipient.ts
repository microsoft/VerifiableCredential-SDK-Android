/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { JweHeader } from "./IJweGeneralJson";

/**
 * JWE recipient object used by the general JSON
 */
export default interface IJweRecipient {

  /**
   * The unprotected (unverified) header.
   */
  header?: JweHeader,

  /**
   * The encrypted key.
   */
  encrypted_key: Buffer
}
