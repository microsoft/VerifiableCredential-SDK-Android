/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import CryptoFactory from '../../plugin/CryptoFactory';
import PrivateKey from '../../keys/PrivateKey';
import PublicKey from '../../keys/PublicKey';
import IJwsCompact from './IJwsCompact';
import IJwsFlatJson from './IJwsFlatJson';
import IJwsGeneralJSon from './IJwsGeneralJSon';
import IJwsSignature from './IJwsSignature';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import IKeyStore, { ISigningOptions ,IEncryptionOptions, IKeyStoreOptions } from '../../keystore/IKeyStore';

/**
 * Definition for a delegate that can verfiy signed data.
 */
type VerifySignatureDelegate = (signedContent: string, signature: string, jwk: PublicKey) => Promise<boolean>;

/**
 * Class for containing JWS token operations.
 * This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
 * Crypto calls always happen via CryptoFactory
 */
export default class JwsToken {

  // used for verification if a JSON Serialized JWS was given
  public readonly signature: string | undefined;

  /**
   * Payload (base64url encoded)
   */
  public payload: string | undefined;
  
  /**
   * Signatures on content
   */
  public signatures: IJwsSignature[] | undefined = [] as IJwsSignature[];

  private options: ISigningOptions | IEncryptionOptions | undefined;
  /**
   * Create an Jws token object
   * @param cryptoFactory Suite of crypto APIs to use
   */
  constructor (options?: ISigningOptions | IEncryptionOptions){
    this.options = options;
  }

  /**
   * Create an Jws token object from a token
   * @param token Base object used to create this token
   * @param cryptoFactory Suite of crypto APIs to use
   */
  public static create (
    token: IJwsFlatJson | IJwsGeneralJSon | IJwsCompact | string, cryptoFactory: CryptoFactory) : JwsToken {
      const jwsToken = new JwsToken(cryptoFactory);
      
      // check for JWS compact format
    if (typeof token === 'string') {
      const parts = token.split('.');
      if (parts.length === 3) {
        jwsToken.payload = parts[1];
        const signature = {} as IJwsSignature;
        signature.protected = jwsToken.setProtected(parts[0]);
        signature.signature = parts[2];
        return jwsToken;
      } else {
        throw new Error(`Invalid compact JWS. Content has ${parts.length} item. Only 3 allowed`);
      }
    }

    // Check for JSON Serialization and reparse content if appropriate
    if (typeof token === 'object') {
      // set payload
      jwsToken.payload = token.payload;

      // Try to handle token as IJwsGeneralJSon
      let decodeStatus: { result: boolean, reason: string } = jwsToken.setGeneralParts(token as IJwsGeneralJSon);
      if (decodeStatus.result) {
          return jwsToken;
      } else {
        console.debug(`Failed parsing as IJwsGeneralJSon. Reasom: ${decodeStatus.reason}`)
      }

      // Try to handle token as IJwsCompact
      decodeStatus = jwsToken.setCompactParts(token as IJwsGeneralJSon);
      if (decodeStatus.result) {
          return jwsToken;
      } else {
        console.debug(`Failed parsing as IJwsCompact. Reasom: ${decodeStatus.reason}`)
      }
      // Try to handle token as IJwsFlatJson
      decodeStatus = jwsToken.setFlatParts(token as IJwsGeneralJSon);
      if (decodeStatus.result) {
          return jwsToken;
      } else {
        console.debug(`Failed parsing as IJwsFlatJson. Reasom: ${decodeStatus.reason}`)
      }

      throw new Error(`The content does not represent a valid jws token`)
    }
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJwsGeneralJSon token
   * @returns true if valid token was parsed
   */
  private setGeneralParts(content: IJwsGeneralJSon): {result: boolean, reason: string} {
    if (content) {
      if (content.payload) {
        this.payload = content.payload;
      } else {
        // manadatory field
        return {result: false, reason: 'missing payload'};
      }

      this.signatures = content.signatures ? content.signatures : undefined;
      return this.isValidToken();
    }

    return {result: false, reason: 'no content passed'};
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJwsFlatJson token
   * @returns true if valid token was parsed
   */
  private setFlatParts(content: IJwsFlatJson): {result: boolean, reason: string} {
    if (content) {
      const signature = {} as IJwsSignature;

      if (content.signature) {
        signature.signature = content.signature;
      } else {
        // manadatory field
        return {result: false, reason: 'missing signature'};
      }

      if (content.protected) {
        signature.protected = this.setProtected(content.protected);
      } 

      if (content.header) {
        signature.header = content.header;
      } 

      if (content.payload) {
        this.payload = content.payload;
      } else {
        // manadatory field
        return {result: false, reason: 'missing payload'};
      }

      this.signatures = [signature];
      return this.isValidToken();
    } 

    return {result: false, reason: 'no content passed'};
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJwsCompact token
   * @returns true if valid token was parsed
   */
  private setCompactParts(content: IJwsCompact): {result: boolean, reason: string} {
    if (content) {
      const signature = {} as IJwsSignature;

      if (content.signature) {
        signature.signature = content.signature;
      } else {
        // manadatory field
        return {result: false, reason: 'missing signature'};
      }

      if (content.protected) {
        signature.protected = this.setProtected(content.protected);
      } else {
        // manadatory field
        return {result: false, reason: 'missing protected'};
      }

      if (content.payload) {
        this.payload = content.payload;
      } else {
        // manadatory field
        return {result: false, reason: 'missing payload'};
      }

      this.signatures = [signature];
      return this.isValidToken();
    } 

    return {result: false, reason: 'no content passed'};
  }

  /**
   * Set the protected header
   * @param protectedHeader to set on the JwsToken object
   */
  private setProtected(protectedHeader: string | {[name: string]: string} ) {
    if (typeof protectedHeader === 'string' ) {
      const json = base64url.decode(protectedHeader);
      return JSON.parse(json) as  {[name: string]: string};
    }

    return protectedHeader;
  }

  /**
   * Check if a valid token was found after decoding
   */
  private isValidToken(): {result: boolean, reason: string} {
    if (!this.payload) {
      return {result: false, reason: 'missing payload'};
    }

    if (!this.signatures) {
      return {result: false, reason: 'missing signatures'};
    }

    if (this.signatures.length == 0) {
      return {result: false, reason: 'signatures array is empty'};
    }

    for (let inx = 0; inx < this.signatures.length; inx++) {
      const signature = this.signatures[inx];
      if (!signature.signature) {
        return {result: false, reason: `signature ${inx} is missing signature`};
      }
      if (!signature.header && !signature.protected) {
        return {result: false, reason: `signature ${inx} is missing header and protected`};
      }
    }

    return {result: true, reason: ''};
  }

  /**
   * Get the keyStore to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getKeyStore(newOptions?: IKeyStoreOptions, manadatory: boolean = true): IKeyStore {
    return this.getOptionsProperty<IKeyStore>('keyStore', newOptions, manadatory);
  }

  /**
   * Get the CryptoFactory to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getCryptoFactory(newOptions?: IKeyStoreOptions, manadatory: boolean = true): CryptoFactory {
    return this.getOptionsProperty<CryptoFactory>('cryptoFactory', newOptions, manadatory);
  }

  /**
   * Get the default protected header to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getProtected(newOptions?: IKeyStoreOptions, manadatory: boolean = false): { [name: string]: string } {
    return this.getOptionsProperty<{ [name: string]: string }>('protected', newOptions, manadatory);
  }

  /**
   * Get the default header to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getHeader(newOptions?: IKeyStoreOptions, manadatory: boolean = false): { [name: string]: string } {
    return this.getOptionsProperty<{ [name: string]: string }>('header', newOptions,manadatory);
  }

  /**
   * Get the algorithm
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getAlgorithm(newOptions?: IKeyStoreOptions, manadatory: boolean = true): Algorithm {
    return this.getOptionsProperty<Algorithm>('algorithm', newOptions, manadatory);
  }

  /**
   * Get the Protected to be used
   * @param propertyName Property name in options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getOptionsProperty<T>(propertyName: string, newOptions?: IKeyStoreOptions,  manadatory: boolean = true): T {
    let newProtected: T | undefined;
    let ctorProtected: T | undefined;

    if (newOptions) {
      newProtected = newOptions[propertyName] as T;
    }
    if (this.options) {
      ctorProtected = this.options[propertyName] as T;
    }

    if (manadatory && !newProtected && !ctorProtected) {
      throw new Error(`The property ${propertyName} is missing from options`);
    }

    return newProtected || ctorProtected as T;
  }

  /**
   * Signs contents using the given private key in JWK format.
   *
   * @param signingKeyReference Reference to the signing key.
   * @param payload to sign.
   * @param format of the final signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in compact JWS format.
   */
  public async sign (signingKeyReference: string, payload: string, format: ProtectionFormat, options?: ISigningOptions): Promise<IJwsGeneralJSon> {
    const keyStore: IKeyStore = this.getKeyStore(options);
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const algorithm: Algorithm = this.getAlgorithm(options);
    const alg: string = this.getJoseAlgorithm(algorithm);

    // TODO support for multiple signatures
    const jwsSignature: IJwsSignature = {} as IJwsSignature;

    // Set payload
    const jwsToken = new JwsToken();

    // Get signing key public key
    const jwk: PublicKey = await keyStore.get(signingKeyReference, false) as PublicKey;

    // Steps according to RTC7515 5.1
    // 2. Compute encoded payload value base64URL(JWS Payload)
    jwsToken.payload = payload;
    const encodedContent = base64url.encode(payload);

    // 3. Compute the headers. 
    jwsSignature.header = this.getHeader(options) || {};
    jwsSignature.protected = this.getProtected(options) || {};

    // Get the required algorithm
    // add required fields if missing
    if (!('alg' in jwsSignature.header) && !('alg' in jwsSignature.protected)) {
      jwsSignature.protected['alg'] = alg;
    }

    if (jwk.kid && 
      !(jwsSignature.header && 'kid' in jwsSignature.header) && 
      !('kid' in jwsSignature.protected)) {
        jwsSignature.protected['kid'] = jwk.kid;
    }

    const protectedUsed = Object.keys(jwsSignature.protected).length > 0;
    
    // 4. Compute BASE64URL(UTF8(JWS Header))
    const encodedProtected = !protectedUsed ? '' : 
      base64url.encode(JSON.stringify(jwsSignature.protected));

    // 5. Compute the signature using data ASCII(BASE64URL(UTF8(JWS protected Header))) || . || . BASE64URL(JWS Payload)
    //    using the "alg" signature algorithm.
    const signatureInput = `${encodedProtected}.${encodedContent}`;

    // TODO define base layer plugable crypto API
    const signature = await (cryptoFactory.getMessageSigners(alg)).sign(signatureInput, jwk);
    // 6. Compute BASE64URL(JWS Signature)
    const encodedSignature = base64url.fromBase64(signature);
    // 8. Create the desired output: BASE64URL(UTF8(JWS Header)) || . BASE64URL(JWS payload) || . || BASE64URL(JWS Signature)
    const jws: IJwsGeneralJSon = {
      format,
      payload,
      signatures: [jwsSignature]
    };
    return jws;
  }

  /**
   * Gets the base64 URL decrypted payload.
   */
  public getPayload (): string {
    return base64url.decode(this.payload as string) as string;
  }

  private getJoseAlgorithm(algorithm: Algorithm): string {
    return '';
  }
}
