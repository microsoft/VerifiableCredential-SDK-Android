/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { JwsToken, CryptoFactory } from '@decentralized-identity/did-auth-jose';

/**
 * Class for creating and managing a claim
 */
export default class JwtClaimDetais {

  /**
   * the contents in the payload of the jwt
   * aka the actual verified claims
   */
  public contents: {[key: string]: string};

 /**
  * the jwsToken representation of the claim details.
  */
  public jwsToken: JwsToken;

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
  constructor (jwsToken: JwsToken) {
    this.jwsToken = jwsToken;
    this.contents = jwsToken.getPayload();
  }

    // TODO: verify that it is a JWS and break down into properties
    // return an error if it's not formatted correctly, or do this in the create method
  public static create (jws: string, cryptoFactory: CryptoFactory) {
    const jwsToken = new JwsToken(jws, cryptoFactory);
    return new JwtClaimDetais(jwsToken);
  }

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
