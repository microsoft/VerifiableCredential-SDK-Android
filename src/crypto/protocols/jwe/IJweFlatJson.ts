/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IJweRecipient from './IJweRecipient';

/**
 * JWS flattened json format
 */
export default interface IJweFlatJson extends IJweRecipient {

  /**
   * The application-specific payload.
   */
  payload: string,
}
