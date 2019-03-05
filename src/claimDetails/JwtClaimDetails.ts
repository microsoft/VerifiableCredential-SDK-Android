/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { JwsToken, CryptoFactory, PrivateKey } from '@decentralized-identity/did-auth-jose';
import Identifier from '../Identifier';
import { ClaimDetails } from '..';

/**
 * Class for creating and managing a claim
 */
export default class JwtClaimDetails implements ClaimDetails {

  /**
   * the contents in the payload of the jwt
   * aka the actual verified claims
   */
  public contents: any | undefined;

 /**
  * the jwsToken representation of the claim details.
  */
  public jwsToken: JwsToken;

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
    return new JwtClaimDetails(jwsToken);
  }

  /**
   * Sign the claim and return a JWT
   */
  public async sign (privateKey: PrivateKey): Promise<string> {
    return this.jwsToken.sign(privateKey);
  }

  /**
   * Verify the claim and return the contents
   */
  public async verify (identifier: Identifier): Promise<any> {
    const publicKey = await identifier.getPublicKey();
    return this.jwsToken.verifySignature(publicKey.publicKeyJwk);
  }

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
   // public revoke(identifier: Identifier) {}

}
