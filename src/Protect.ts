/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { Secp256k1CryptoSuite, CryptoFactory, JwsToken, RsaCryptoSuite } from '@decentralized-identity/did-auth-jose';
import UserAgentError from './UserAgentError';
import IKeyStore from './keystores/IKeyStore';
import { PublicKey } from './types';

/**
 * Class for Signing and Verifying payloads using keystore and Identifier Documents respectively.
 */
export default class Protect {

  /**
   * Sign the body with the reference key in keystore.
   * @param body JSON body to be signed
   * @param keyStorageReference reference string for key in keyStore
   * @param keyStore keyStore that holds the key.
   */
  public static async sign (
    body: string,
    keyStorageReference: string,
    keyStore: IKeyStore
  ): Promise<string> {
    const cryptoFactory = new CryptoFactory([new Secp256k1CryptoSuite(), new RsaCryptoSuite()]);
    const token = new JwsToken(body, cryptoFactory);
    // Get the key
    const jwk: any = await keyStore.get(keyStorageReference)
    .catch((err) => {
      throw new UserAgentError(`The key referenced by '${keyStorageReference}' is not available: '${err}'`);
    });

    switch (jwk.kty.toUpperCase()) {
      case 'RSA':
        jwk.defaultSignAlgorithm = 'RS256';
        break;

      case 'EC':
        jwk.defaultSignAlgorithm = 'ES256K';
        break;

      default:
        throw new UserAgentError(`The key type '${jwk.kty}' is not supported.`);
    }

    const signedToken = await token.sign(jwk);
    return signedToken;
  }

  /**
   * Verify the jws
   * @param jws token to be verified
   * @param jwk Public Key to be used to verify
   */
  public static async verify (jws: string, publicKeys: Array<PublicKey>) {

    const cryptoFactory = new CryptoFactory([new Secp256k1CryptoSuite(), new RsaCryptoSuite()]);
    const token = new JwsToken(jws, cryptoFactory);
    const headers = token.getHeader();
    const filteredpublicKeys = publicKeys.filter(key => headers.kid === key.id);

    if (filteredpublicKeys.length === 0) {
      throw new UserAgentError(`No Public Key '${headers.kid}' in the Identifier Document`);
    }

    const publicKey = filteredpublicKeys[0].publicKeyJwk;

    switch (publicKey.kty.toUpperCase()) {
      case 'RSA':
        publicKey.defaultSignAlgorithm = 'RS256';
        break;

      case 'EC':
        publicKey.defaultSignAlgorithm = 'ES256K';
        break;

      default:
        throw new UserAgentError(`The key type '${publicKey.kty}' is not supported.`);
    }

    const body = await token.verifySignature(publicKey)
    .catch(err => {
      throw new UserAgentError(`JWS Token cannot be verified by public key: '${err}`);
    });

    return body;
  }
}
