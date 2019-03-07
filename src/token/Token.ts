/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { PrivateKey, PublicKey } from '@decentralized-identity/did-auth-jose';

/**
 * ClaimDetails Types
 */
export enum ClaimDetailsType {
  jws= 'jws',
  vc= 'vc'
}

/**
 * Interface defining methods and properties for a Token object
 */
export default interface Token {

  /**
   * the contents for the claim
   */
  contents: any;

  /**
   * Return the extracted contents of the Token.
   */
  extractContents (): any;

  /**
   * Sign the claim and return a JWT
   */
  sign (privateKey: PrivateKey): Promise<string>;

  /**
   * Verify the claim and return the contents
   */
  verify (publicKey: PublicKey): Promise<any>;

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
  // revoke (identifier: Identifier): void;

}
