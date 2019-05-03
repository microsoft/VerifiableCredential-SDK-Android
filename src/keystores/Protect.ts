/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { FlatJsonJws, Secp256k1CryptoSuite, CryptoFactory, JwsToken, RsaCryptoSuite } from '@decentralized-identity/did-auth-jose';
import UserAgentError from '../UserAgentError';
import IKeyStore from './IKeyStore';
import { PublicKey } from '../types';

 /**
  * Class to model protection mechanisms
  */
export default class Protect {
  /**
   * Sign the body for the registar
   * @param body Body to sign
   */
  public static async sign (
    body: string,
    keyStorageReference: string,
    keyStore: IKeyStore
  ): Promise<string> {
    const cryptoFactory = new CryptoFactory([new Secp256k1CryptoSuite(), new RsaCryptoSuite()]);
    const token = new JwsToken(body, cryptoFactory);
    // Get the key
    const jwk: any = await keyStore.getKey(keyStorageReference)
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

    const signedRegistrationRequest: FlatJsonJws = await token.signAsFlattenedJson(jwk, {
      header: {
        alg: jwk.defaultSignAlgorithm,
        kid: jwk.kid,
        operation: 'create',
        proofOfWork: '{}'
      }
    });

    return JSON.stringify(signedRegistrationRequest);
  }

  /**
   * Verify the jws
   * @param jws token to be verified
   * @param jwk Public Key to be used to verify
   */
  public static async verify (jws: string, publicKeys: PublicKey[]) {

    const cryptoFactory = new CryptoFactory([new Secp256k1CryptoSuite(), new RsaCryptoSuite()]);
    const token = new JwsToken(jws, cryptoFactory);
    const headers = token.getHeader();
    const filteredpublicKeys = publicKeys.filter(key => headers.kid === key.id);

    if (filteredpublicKeys.length === 0) {
      throw new UserAgentError(`No Public Key '${headers.kid}'`);
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

    return token.verifySignature(publicKey)
    .catch((error: any) => {
      throw new UserAgentError(`JWS Token cannot be verified by public key: '${error}'`);
    });
  }
}
