/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { JwsToken, CryptoFactory, RsaCryptoSuite, Secp256k1CryptoSuite, PrivateKey } from '@decentralized-identity/did-auth-jose';
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
   * Constructs an instance of the ClaimDetails Class
   * @param jwsToken a jwsToken Object.
   */
  constructor (jwsToken: JwsToken) {
    this.jwsToken = jwsToken;
    this.contents = jwsToken.getPayload();
  }

  /**
   * Create a new instance of JwtClaimDetails.
   * @param content either the signed payload represented as a string or the payload object to be signed.
   * @param options optional options such as the cryptoSuites used to sign/verify JWSToken.
   * TODO: decide if cryptofactory is an advanced option and have defaults instead (RSA and EC)
   */
  public static create (content: string | Object, options?: any): JwtClaimDetails {
    const cryptoFactory = new CryptoFactory(options.cryptoSuites || [new RsaCryptoSuite(), new Secp256k1CryptoSuite()]);
    const jwsToken = new JwsToken(content, cryptoFactory);
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
  public async verify (identifier: Identifier): Promise<string> {
    const publicKey = await identifier.getPublicKey();
    return this.jwsToken.verifySignature(publicKey.publicKeyJwk);
  }

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
   // public revoke(identifier: Identifier) {}

}
