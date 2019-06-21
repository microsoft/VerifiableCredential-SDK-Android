/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from '../plugin/CryptoFactory';
import PublicKey from '../keys/PublicKey';
import EcPublicKey from '../keys/ec/EcPublicKey';
import { CryptoAlgorithm } from '../keyStore/IKeyStore';
import { SubtleCrypto } from 'webcrypto-core';
import W3cCryptoApiConstants from './W3cCryptoApiConstants';
import JoseConstants from '../protocols/jose/JoseConstants';

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
  public static getSubtleCryptoForAlgorithm(cryptoFactory: CryptoFactory, algorithm: any): SubtleCrypto {
    const jwa = CryptoHelpers.webCryptoToJwa(algorithm)
    switch (algorithm.name.toUpperCase()) {
      case 'RSASSA-PKCS1-V1_5':
      case 'ECDSA':
        return cryptoFactory.getMessageSigner(jwa);
        case 'RSA-OAEP': 
        case 'RSA-OAEP-256': 
        return cryptoFactory.getKeyEncrypter(jwa);
      case 'AES-GCM':
        return cryptoFactory.getSymmetricEncrypter(jwa);
      case 'HMAC':
        return cryptoFactory.getMessageAuthenticationCodeSigners(jwa);
      case 'SHA-256':
      case 'SHA-384':
      case 'SHA-512':
        return cryptoFactory.getMessageDigest(jwa);
    }

    throw new Error(`Algorithm '${JSON.stringify(algorithm)}' is not supported`);
  }

  /**
   * Map the JWA algorithm to the W3C crypto API algorithm.
   * The method restricts the supported algorithms. This can easily be extended.
   * Based on https://www.w3.org/TR/WebCryptoAPI/ A. Mapping between JSON Web Key / JSON Web Algorithm
   * @param jwaAlgorithmName Requested algorithm
   */
  public static jwaToWebCrypto(jwa: string, ...args: any): any {
    const regex = new RegExp('\\d+');
    let matches:RegExpExecArray;

    switch (jwa.toUpperCase()) {
      case JoseConstants.Rs256:
      case JoseConstants.Rs384:
      case JoseConstants.Rs512:
        matches = <RegExpExecArray>regex.exec(jwa);
        return { name: W3cCryptoApiConstants.RsaSsaPkcs1V15, hash: { name: `SHA-${CryptoHelpers.getRegexMatch(<RegExpExecArray>matches, 0)}`} };
      case JoseConstants.RsaOaep: 
      case JoseConstants.RsaOaep256: 
        return { name: 'RSA-OAEP', hash: 'SHA-256' };
      case JoseConstants.AesGcm128:
      case JoseConstants.AesGcm192:
      case JoseConstants.AesGcm256:
        const iv = args[0];
        const aad = args[1];
        matches = <RegExpExecArray>regex.exec(jwa);
        const length = parseInt(CryptoHelpers.getRegexMatch(<RegExpExecArray>matches, 0));
        return { name: W3cCryptoApiConstants.AesGcm, iv: iv, additionalData: aad, tagLength: 128,  length: length };
      case JoseConstants.Es256K:
        return { name: 'ECDSA', namedCurve: 'P-256K', hash: { name: 'SHA-256' }, format: 'DER' };
    }

    throw new Error(`Algorithm ${JSON.stringify(jwa)} is not supported`);
  }

  /**
   * Maps the subtle crypto algorithm name to the JWA name
   * @param algorithmName Requested algorithm
   * @param hash Optional hash for the algorithm
   */
  public static webCryptoToJwa(algorithm: any): string {
    const hash = algorithm.hash || 'SHA-256';
    switch (algorithm.name.toUpperCase()) {
      case 'RSASSA-PKCS1-V1_5':
        return `RS${CryptoHelpers.getHash(hash)}`;
      case 'ECDSA':
          return `ES256K`;
      case 'RSA-OAEP-256':
            return 'RSA-OAEP-256'; 
      case 'RSA-OAEP': 
          return `RSA-OAEP-${CryptoHelpers.getHash(hash)}`;
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
  public static getKeyImportAlgorithm(algorithm: CryptoAlgorithm, jwk: PublicKey | JsonWebKey): string | RsaHashedImportParams | EcKeyImportParams | HmacImportParams | DhImportKeyParams {
    const hash = (<any>algorithm).hash || 'SHA-256';
    const name = algorithm.name;
    switch (algorithm.name.toUpperCase()) {
      case 'RSASSA-PKCS1-V1_5':
        return  <RsaHashedImportParams>{ name, hash: {name: "SHA-256"} };
      case 'HMAC':
      case 'SHA-256':
      case 'SHA-384':
      case 'SHA-512':
          return <RsaHashedImportParams>{ name, hash };
      case 'ECDSA':
      case 'ECDH':
          return <EcKeyImportParams>{ name, namedCurve: (<EcPublicKey>jwk).crv };
      case 'RSA-OAEP': 
      case 'RSA-OAEP-256': 
          return {name, hash: 'SHA-256'}
      case 'AES-GCM':
          return <RsaHashedImportParams>{ name };
      }
      throw new Error(`Algorithm '${JSON.stringify(algorithm)}' is not supported`);
    }        

  private static getHash(hash: any) {
    if (hash.name) {
      return (hash.name).toUpperCase().replace('SHA-', '');
    }
    return (hash || 'SHA-256').toUpperCase().replace('SHA-', '');
  }

  private static getRegexMatch(matches: RegExpExecArray, index: number): string {
    return matches[index];
  }
}
