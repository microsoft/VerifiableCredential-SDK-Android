/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';

/**
 * Interface defining methods and properties to
 * be implemented by specific claim objects
 */
export default interface ClaimDetails {

  /**
   * the contents for the claim
   */
  contents: any;

  /**
   * signature on the claim
   */
  signature: string;

  /**
   * Check to see if the claim has expired
   */
  hasExpired (): boolean;

  /**
   * Sign the claim and return a JWT
   */
  sign (identifier: Identifier, privateKey: any): Promise<string>;

  /**
   * Verify the claim and return the contents
   */
  verify (identifier: Identifier): Promise<any>;

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
  revoke (identifier: Identifier): void;

}
