/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from '../plugin/CryptoFactory';
import ISubtleCrypto from '../plugin/ISubtleCrypto';
import PublicKey from '../keys/PublicKey';
import EcPublicKey from '../keys/ec/EcPublicKey';

/**
 * Crypto helpers support for plugable crypto layer
 */
export default class CryptoHelpers {

  /**
   * The API which implements the requested algorithm
   * @param cryptoFactory Crypto suite
   * @param algorithmName Requested algorithm
   * @param hash Optional hash for the algorithm
   */
  public static getSubtleCrypto(cryptoFactory: CryptoFactory, algorithm: any): ISubtleCrypto {
    const jwa = CryptoHelpers.toJwa(algorithm)
    switch (algorithm.name.toUpperCase()) {
      case 'RSASSA-PKCS1-V1_5':
      case 'ECDSA':
        return cryptoFactory.getMessageSigner(jwa);
        case 'RSA-OAEP': 
        return cryptoFactory.getKeyEncrypter(jwa);
      case 'ECDH':
      case 'DH':
                return cryptoFactory.getSharedKeyEncrypter(jwa);
      case 'AES-GCM':
        return cryptoFactory.getSymmetricEncrypter(jwa);
      case 'HMAC':
        return cryptoFactory.getMacSigner(jwa);
        case 'SHA-256':
        case 'SHA-384':
        case 'SHA-512':
          return cryptoFactory.getMessageDigest(jwa);
    }

    throw new Error(`Algorithm '${JSON.stringify(algorithm)}' is not supported`);
  }

  /**
   * Maps the subtle crypto algorithm name to the JWA name
   * @param algorithmName Requested algorithm
   * @param hash Optional hash for the algorithm
   */
  public static toJwa(algorithm: any): string {
    const hash = algorithm.hash || 'SHA-256';
    switch (algorithm.name.toUpperCase()) {
      case 'RSASSA-PKCS1-V1_5':
        return `RS${CryptoHelpers.getHash(hash)}`;
      case 'ECDSA':
          return `P-256K`;
      case 'RSA-OAEP': 
        return `RSA-OAEP-${CryptoHelpers.getHash(hash)}`;
      case 'ECDH':
          return `ECDH-ES`;
      case 'AES-GCM':
        const length = algorithm.length || 128;
        return `A${length}GCMKW`;
      case 'HMAC':
        return `HS${CryptoHelpers.getHash(hash)}`;

        case 'SHA-256':
        case 'SHA-384':
        case 'SHA-512':
            return `SHA${CryptoHelpers.getHash(hash)}`;
          }

    throw new Error(`Algorithm '${JSON.stringify(algorithm)}' is not supported`);
  }
  
  /**
   * Derive the key import algorithm
   * @param algorithm used for signature
   */
  public static getKeyImportAlgorithm(algorithm: RsaPssParams | EcdsaParams | AesCmacParams, jwk: PublicKey): string | RsaHashedImportParams | EcKeyImportParams | HmacImportParams | DhImportKeyParams {
    const hash = (<any>algorithm).hash || 'SHA-256';
    const name = algorithm.name;
    switch (algorithm.name.toUpperCase()) {
      case 'RSASSA-PKCS1-V1_5':
      case 'RSA-OAEP': 
      case 'HMAC':
      case 'SHA-256':
      case 'SHA-384':
      case 'SHA-512':
          return <RsaHashedImportParams>{ name, hash };
      case 'ECDSA':
      case 'ECDH':
          return <EcKeyImportParams>{ name, namedCurve: (<EcPublicKey>jwk).crv };
      case 'AES-GCM':
          return <RsaHashedImportParams>{ name };
      }
      throw new Error(`Algorithm '${JSON.stringify(algorithm)}' is not supported`);
    }        

  private static getHash(hash: string) {
    return (hash || 'SHA-256').toUpperCase().replace('SHA-', '');
  }
}
