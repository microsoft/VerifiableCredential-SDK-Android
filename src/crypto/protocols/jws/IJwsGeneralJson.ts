/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IJwsSignature from './IJwsSignature';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import { TSMap } from 'typescript-map'

/**
 * Defines a header in JWS
 */
export type JwsHeader = TSMap<string, string>;


/**
 * JWS general json format
 */
export default interface IJwsGeneralJson {

  /**
   * The application-specific non-encoded payload.
   */
  payload: string,

  /**
   * The signatures
   */
  signatures: IJwsSignature[],

  /**
   * The serialization format
   */
  format: ProtectionFormat
}
