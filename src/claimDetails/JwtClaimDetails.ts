import { Identifier } from '..';

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Class for creating and managing a claim
 */
export default class JwtClaimDetais {

  /**
   * the contents in the payload of the jwt
   * aka the actual verified claims
   */
 // public contents: {[key: string]: string};

 /**
  * the signature on the jwt
  */
// public signature: string;

  /**
   * the issuer of the claim
   */
// public issuer: string;

  /**
   * the subject of the claim
   */
// public subject: string;

  /**
   * expiration of the claim
   */
// public expiration: string;

  /**
   * Constructs an instance of the Claim
   * class using the provided jwt or claimObj
   */
  // constructor (claim: ClaimObj | string) {

  //   if (typeof claim === 'string') {
  //     this.jwt = claim;
  //   } else {
  //     this.jwt = claim.jwt;
  //     this.uiRef = claim.uiRef;
  //   }

    // TODO: verify that it is a JWS and break down into properties
    // return an error if it's not formatted correctly, or do this in the create method

  /**
   * Creates a new claim object
   */
  // public static create (jwt: string, uiRef: any): Claim {
  //   const claim = new JwtClaim({ jwt, uiRef });
  //   return claim;
  // }

  /**
   * Check to see if the claim has expired
   */
// public hasExpired(): boolean {}

  /**
   * Sign the claim and return a JWT
   */
  // public async sign(identifier: Identifier, privateKey: PrivateKey): Promise<string> {}

  /**
   * Verify the claim and return the contents
   */
  // public async verify(identifier: Identifier): Promise<any> {}

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
  // public revoke(identifier: Identifier) {}

}
