/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import { PrivateKey } from '@decentralized-identity/did-auth-jose';

/**
 * Interface defining methods and properties for a ClaimDetails object
 */
export default interface ClaimDetails {

  /**
   * the contents for the claim
   */
  contents: any;

  /**
   * Sign the claim and return a JWT
   */
  sign (privateKey: PrivateKey): Promise<string>;

  /**
   * Verify the claim and return the contents
   */
  verify (identifier: Identifier): Promise<any>;

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
  // revoke (identifier: Identifier): void;

}
