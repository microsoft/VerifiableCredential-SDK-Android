/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { JwsToken, CryptoFactory, RsaCryptoSuite, Secp256k1CryptoSuite, PrivateKey, PublicKey } from '@decentralized-identity/did-auth-jose';
import Token from './Token';

/**
 * Class for creating and managing a JWT-formed claims
 */
export default class Jwt implements Token {

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
   * Constructs an instance of the JWT Class
   * @param jwsToken a jwsToken Object.
   */
  constructor (jwsToken: JwsToken) {
    this.jwsToken = jwsToken;
    this.contents = jwsToken.getPayload();
  }

  /**
   * Create a new instance of JWT Class.
   * @param content either the signed payload represented as a string or the payload object to be signed.
   * @param options optional options such as the cryptoSuites used to sign/verify JwsToken.
   * TODO: decide if cryptofactory is an advanced option and have defaults instead (RSA and EC)
   */
  public static create (content: string | Object, cryptoSuites?: any): Jwt {
    const cryptoFactory = new CryptoFactory(cryptoSuites || [new RsaCryptoSuite(), new Secp256k1CryptoSuite()]);
    const jwsToken = new JwsToken(content, cryptoFactory);
    return new Jwt(jwsToken);
  }

  /**
   * Returns the extracted contents of the token.
   */
  public extractContents () {
    return this.contents;
  }

  /**
   * Sign the claim and return a JWT
   */
  public async sign (privateKey: PrivateKey): Promise<string> {
    return this.jwsToken.sign(privateKey);
  }

  /**
   * Verify the claim and return the contents
   * @return the stringified contents of the token.
   */
  public async verify (publicKey: PublicKey): Promise<string> {
    return this.jwsToken.verifySignature(publicKey);
  }

  /**
   * The issuer of the claim revokes the claim
   * TODO: figure out how
   */
   // public revoke(identifier: Identifier) {}

}
