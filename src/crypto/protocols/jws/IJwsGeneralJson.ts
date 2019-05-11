/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IJwsSignature from './IJwsSignature';

/**
 * JWS general json format
 */
export default interface IJwsFlatJson extends IJwsSignature {

  /**
   * The application-specific non-encoded payload.
   */
  payload: string,

  /**
   * The signatures
   */
  signatures: IJwsSignature[]
};
