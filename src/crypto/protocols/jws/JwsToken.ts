/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import CryptoFactory from '../../plugin/CryptoFactory';
import PublicKey from '../../keys/PublicKey';
import IJwsCompact from './IJwsCompact';
import IJwsFlatJson from './IJwsFlatJson';
import IJwsGeneralJson from './IJwsGeneralJson';
import IJwsSignature from './IJwsSignature';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import IKeyStore, { ISigningOptions ,IEncryptionOptions, IKeyStoreOptions, CryptoAlgorithm } from '../../keystore/IKeyStore';
import CryptoHelpers from '../../utilities/CryptoHelpers';

type Header = {[name: string]: string};

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
  public signatures: IJwsSignature[] | undefined = <IJwsSignature[]>[];

  private options: ISigningOptions | IEncryptionOptions | undefined;
  /**
   * Create an Jws token object
   * @param options Set of jws token options
   */
  constructor (options?: ISigningOptions | IEncryptionOptions){
    this.options = options;
  }

  /**
   * Create an Jws token object from a token
   * @param token Base object used to create this token
   * @param options Set of jws token options
   */
  public static create (
    token: IJwsFlatJson | IJwsGeneralJson | IJwsCompact | string, options?: ISigningOptions | IEncryptionOptions) : JwsToken {
      const jwsToken = new JwsToken(options);
      
      // check for JWS compact format
    if (typeof token === 'string') {
      const parts = token.split('.');
      if (parts.length === 3) {
        jwsToken.payload = parts[1];
        const signature = <IJwsSignature>{};
        signature.protected = jwsToken.setProtected(parts[0]);
        signature.signature = base64url.toBuffer(parts[2]);
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
      let decodeStatus: { result: boolean, reason: string } = jwsToken.setGeneralParts(<IJwsGeneralJson>token);
      if (decodeStatus.result) {
          return jwsToken;
      } else {
        console.debug(`Failed parsing as IJwsGeneralJSon. Reasom: ${decodeStatus.reason}`)
      }

      // Try to handle token as IJwsCompact
      decodeStatus = jwsToken.setCompactParts(<IJwsCompact>token);
      if (decodeStatus.result) {
          return jwsToken;
      } else {
        console.debug(`Failed parsing as IJwsCompact. Reasom: ${decodeStatus.reason}`)
      }

      // Try to handle token as IJwsFlatJson
      decodeStatus = jwsToken.setFlatParts(<IJwsFlatJson>token);
      if (decodeStatus.result) {
          return jwsToken;
      } else {
        console.debug(`Failed parsing as IJwsFlatJson. Reasom: ${decodeStatus.reason}`);
      }
    }
    throw new Error(`The content does not represent a valid jws token`);
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJwsGeneralJSon token
   * @returns true if valid token was parsed
   */
  private setGeneralParts(content: IJwsGeneralJson): {result: boolean, reason: string} {
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
      const signature = <IJwsSignature>{};

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
      const signature = <IJwsSignature>{};

      if (content.signature) {
        signature.signature = base64url.toBuffer(content.signature);
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
  private setProtected(protectedHeader: string | Header ) {
    if (typeof protectedHeader === 'string' ) {
      const json = base64url.decode(protectedHeader);
      return <Header>JSON.parse(json);
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
   * Get the default protected header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getProtected(newOptions?: IKeyStoreOptions, manadatory: boolean = false): { [name: string]: string } {
    return this.getOptionsProperty<{ [name: string]: string }>('protected', newOptions, manadatory);
  }

  /**
   * Get the default header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getHeader(newOptions?: IKeyStoreOptions, manadatory: boolean = false): { [name: string]: string } {
    return this.getOptionsProperty<{ [name: string]: string }>('header', newOptions,manadatory);
  }

  /**
   * Get the algorithm from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getAlgorithm(newOptions?: IKeyStoreOptions, manadatory: boolean = true): CryptoAlgorithm {
    return this.getOptionsProperty<CryptoAlgorithm>('algorithm', newOptions, manadatory);
  }

  /**
   * Get the Protected to be used from the options
   * @param propertyName Property name in options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getOptionsProperty<T>(propertyName: string, newOptions?: IKeyStoreOptions,  manadatory: boolean = true): T {
    let newProtected: T | undefined;
    let ctorProtected: T | undefined;

    if (newOptions) {
      newProtected = <T>newOptions[propertyName];
    }
    if (this.options) {
      ctorProtected = <T>this.options[propertyName];
    }

    if (manadatory && !newProtected && !ctorProtected) {
      throw new Error(`The property ${propertyName} is missing from options`);
    }

    return newProtected || <T>ctorProtected;
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
  public async sign (signingKeyReference: string, payload: string, format: ProtectionFormat, options?: ISigningOptions): Promise<IJwsGeneralJson> {
    const keyStore: IKeyStore = this.getKeyStore(options);
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const algorithm: CryptoAlgorithm = this.getAlgorithm(options);
    const alg: string = CryptoHelpers.toJwa(algorithm);

    // tslint:disable-next-line:no-suspicious-comment
    // TODO support for multiple signatures
    const jwsSignature: IJwsSignature = <IJwsSignature>{};

    // Set payload
    const jwsToken = new JwsToken();

    // Get signing key public key
    const jwk: PublicKey = await <Promise<PublicKey>>keyStore.get(signingKeyReference, true);

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

    // call base layer plugable crypto API for signing with a key reference
    const signature = await (cryptoFactory.getMessageSigner(alg)).signByKeyStore(algorithm, signingKeyReference, Buffer.from(signatureInput));
    
    // Compose result
    jwsSignature.signature = Buffer.from(signature);
    return <IJwsGeneralJson> {
      format,
      payload,
      signatures: [jwsSignature]
    };
  }

  /**
   * Gets the base64 URL decrypted payload.
   */
  public getPayload (): string {
    return <string>base64url.decode(<string>this.payload);
  }
}
