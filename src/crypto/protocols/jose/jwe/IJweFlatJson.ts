/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IJweRecipient from './IJweRecipient';
import IJweBase from './IJweBase';
import { JweHeader } from './IJweGeneralJson';

/**
 * JWS flattened json format
 */
export default interface IJweFlatJson extends IJweBase {

  /**
   * The encrypted key.
   */
  encrypted_key: Buffer,

  /**
   * The header.
   */
  header: JweHeader
}
