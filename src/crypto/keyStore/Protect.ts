/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoFactory from '../CryptoFactory';
import PrivateKey from '../keys/PrivateKey';
import IKeyStore, { ISigningOptions, IEncryptionOptions } from './IKeyStore';
import { ProtectionFormat } from './ProtectionFormat';

 /**
  * Class to model protection mechanisms
  */
export default class Protect {
  /**
   * Sign the payload
   * @param keyStorageReference used to reference the signing key
   * @param payload to sign
   * @param format Signature format
   * @param signingOptions Set of signing options
   */
  public static async sign (
    keyStorageReference: string,
    payload: string | Buffer,
    format: ProtectionFormat,
    signingOptions: ISigningOptions
  ): Promise<string> {

    const token = new JwsToken(payload, signingOptions.cryptoFactory);
    // Get the key
    const jwk: any = await signingOptions.keyStore.get(keyStorageReference, false)
    .catch((err) => {
      throw new Error(`The key referenced by '${keyStorageReference}' is not available: '${err}'`);
    });

    switch (jwk.kty.toUpperCase()) {
      case 'RSA':
        jwk.defaultSignAlgorithm = 'RS256';
        break;

      case 'EC':
        jwk.defaultSignAlgorithm = 'ES256K';
        break;

      default:
        throw new Error(`The key type '${jwk.kty}' is not supported.`);
    }

    switch (format) {
      case ProtectionFormat.JwsCompactJson:
        return token.sign(jwk, tokenHeaderParameters);

      case ProtectionFormat.JwsFlatJson:
        const flatSignature: FlatJsonJws = await token.signAsFlattenedJson(jwk, tokenHeaderParameters);
        return JSON.stringify(flatSignature);
      default:
        throw new Error(`Non signature format passed: ${format.toString()}`);
    }
  }

  /**
   * Decrypt the data with the key referenced by keyReference.
   * @param keyStorageReference Reference to the key used for signature.
   * @param cipher Data to decrypt
   * @param encryptionOptions Set of encryption options
   * @returns The plain text message
   */
  public static async decrypt (keyStorageReference: string, cipher: string | Buffer,
    encryptionOptions: IEncryptionOptions): Promise<string> {
    // Get the key
    const jwk: PrivateKey = await (encryptionOptions.keyStore.get(keyStorageReference, false) as Promise<PrivateKey>)
    .catch((err) => {
      throw new Error(`The key referenced by '${keyStorageReference}' is not available: '${err}'`);
    });

    const jweToken = encryptionOptions.cryptoFactory.constructJwe(cipher);
    const payload = await jweToken.decrypt(jwk);
    return payload;
  }
}
