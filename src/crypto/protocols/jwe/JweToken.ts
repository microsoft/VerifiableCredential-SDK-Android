/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import CryptoFactory from '../../plugin/CryptoFactory';
import PublicKey from '../../keys/PublicKey';
import IJweCompact from './IJweCompact';
import IJweFlatJson from './IJweFlatJson';
import IJweGeneralJson, { JweHeader } from './IJweGeneralJson';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import IKeyStore, { ISigningOptions ,IEncryptionOptions, IKeyStoreOptions, CryptoAlgorithm } from '../../keystore/IKeyStore';
import CryptoHelpers from '../../utilities/CryptoHelpers';
import SubtleCryptoExtension from '../../plugin/SubtleCryptoExtension';
import JweRecipient from './JweRecipient';
import { TSMap } from 'typescript-map'

/**
 * Class for containing Jwe token operations.
 * This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
 * Crypto calls always happen via CryptoFactory
 */
export default class JweToken implements IJweGeneralJson {
  /**
   * The protected header.
   */
  public protected: JweHeader = new TSMap<string, string>();

  /**
   * The unprotected header.
   */
  public unprotected: JweHeader = new TSMap<string, string>();
  
  /**
   * The initial vector.
   */
  iv: Buffer = Buffer.from('');

  /**
   * The additional authenticated data.
   */
  public aad: Buffer = Buffer.from('');

  /**
   * The encrypted data.
   */
  public ciphertext: Buffer = Buffer.from('');

  /**
   * The authentication tag used by GCM.
   */
  public tag: Buffer = Buffer.from('');

  /**
   * Signatures on content
   */
  public recipients: JweRecipient[] = [];

  /**
   * Get the request serialization format
   */
  public format: ProtectionFormat = ProtectionFormat.JweGeneralJson;

  // Options passed into the constructor
  private options: ISigningOptions | IEncryptionOptions | undefined;

  /**
   * Create an Jwe token object
   * @param options Set of Jwe token options
   */
  constructor (options?: ISigningOptions | IEncryptionOptions) {
    this.options = options;
  }

  /**
   * Serialize a Jwe token object from a token
   * @param format Optional specify the serialization format. If not specified, use default format.
   */
  public serialize (format?: ProtectionFormat): string {
    if (format === undefined) {
      format = this.format;
    }

      switch (format) {
        case ProtectionFormat.JweGeneralJson:
          return JweToken.serializeJweGeneralJson(this);          
        case ProtectionFormat.JweCompactJson: 
          return JweToken.serializeJweCompact(this);
        case ProtectionFormat.JweFlatJson: 
          return JweToken.serializeJweFlatJson(this);
    }
    
    throw new Error(`The format '${this.format}' is not supported`);
  }

  /**
   * Serialize a Jwe token object from a token in General Json format
   * @param token Jwe base object
   */
  private static serializeJweGeneralJson (token: JweToken): string {
    const Jwe = {
      payload: base64url.encode(<string>token.payload),
      signatures: <any[]>[]
    }

    for (let inx = 0; inx < token.signatures.length; inx ++) {
      const tokenSignature: JweRecipient = token.signatures[inx];
      const JweRecipient: any = {
        signature:  base64url.encode(tokenSignature.signature),
      };
      if (JweToken.headerHasElements(tokenSignature.protected)) {
        JweRecipient.protected = JweToken.encodeHeader(<JweHeader>tokenSignature.protected);
      }
      if (JweToken.headerHasElements(tokenSignature.header)) {
        JweRecipient.header = JweToken.encodeHeader(<JweHeader>tokenSignature.header, false);
      } 
      if (!JweRecipient.protected && ! JweRecipient.header) {
        throw new Error(`Signature ${inx} is missing header and protected`);
      }
      Jwe.signatures.push(JweRecipient);
    }

    return JSON.stringify(Jwe);
  }

  /**
   * Serialize a Jwe token object from a token in Flat Json format
   * @param token Jwe base object
   */
  private static serializeJweFlatJson (token: JweToken): string {
    const Jwe: any = {
      payload: base64url.encode(<string>token.payload)
    }

    if (JweToken.headerHasElements(token.signatures[0].protected)) {
      Jwe.protected = JweToken.encodeHeader(<JweHeader>token.signatures[0].protected);
    }
    if (JweToken.headerHasElements(token.signatures[0].header)) {
      Jwe.header = JweToken.encodeHeader(<JweHeader>token.signatures[0].header, false);
    }
    Jwe.signature = base64url.encode(token.signatures[0].signature);
    return JSON.stringify(Jwe);
  }

  /**
   * Serialize a Jwe token object from a token in Compact format
   * @param token Jwe base object
   */
  private static serializeJweCompact (token: JweToken): string {

    let encodedProtected: string = '';
    if (JweToken.headerHasElements(token.signatures[0].protected)) {
      encodedProtected = JweToken.encodeHeader(<JweHeader>token.signatures[0].protected);
    }

    const encodedpayload = base64url.encode(<string>token.payload);
    const encodedSignature = base64url.encode(token.signatures[0].signature);
    return `${encodedProtected}.${encodedpayload}.${encodedSignature}`;
  }
  
  /**
   * Create an Jwe token object from a token
   * @param token Base object used to create this token
   * @param options Set of Jwe token options
   */
  public static create (
    token: IJweFlatJson | IJweGeneralJson | IJweCompact | string, options?: ISigningOptions | IEncryptionOptions) : JweToken {
      const JweToken = new JweToken(options);
      
      // check for Jwe compact format
    if (typeof token === 'string') {
      const parts = token.split('.');
      if (parts.length === 3) {
        JweToken.payload = parts[1];
        const signature = new JweRecipient();
        signature.protected = JweToken.setProtected(parts[0]);
        signature.signature = base64url.toBuffer(parts[2]);
        return JweToken;
      } else {
        throw new Error(`Invalid compact Jwe. Content has ${parts.length} item. Only 3 allowed`);
      }
    }

    // Check for JSON Serialization and reparse content if appropriate
    if (typeof token === 'object') {
      // set payload
      JweToken.payload = token.payload;

      // Try to handle token as IJweGeneralJSon
      let decodeStatus: { result: boolean, reason: string } = JweToken.setGeneralParts(<IJweGeneralJson>token);
      if (decodeStatus.result) {
          return JweToken;
      } else {
        console.debug(`Failed parsing as IJweGeneralJSon. Reasom: ${decodeStatus.reason}`)
      }

      // Try to handle token as IJweCompact
      decodeStatus = JweToken.setCompactParts(<IJweCompact>token);
      if (decodeStatus.result) {
          return JweToken;
      } else {
        console.debug(`Failed parsing as IJweCompact. Reasom: ${decodeStatus.reason}`)
      }

      // Try to handle token as IJweFlatJson
      decodeStatus = JweToken.setFlatParts(<IJweFlatJson>token);
      if (decodeStatus.result) {
          return JweToken;
      } else {
        console.debug(`Failed parsing as IJweFlatJson. Reasom: ${decodeStatus.reason}`);
      }
    }
    throw new Error(`The content does not represent a valid Jwe token`);
  }

  /**
   * Return true if the header has elements
   * @param header to test
   */
  private static headerHasElements(header: JweHeader | undefined): boolean {
    if (!header) {
      return false;
    }
    return header.length > 0;
  }

  /**
   * Encode the header to JSON and base 64 url
   * @param header to encode
   * @param toBase64Url is true when result needs to be base 64 url
   */
  private static encodeHeader(header: JweHeader, toBase64Url: boolean = true): string {
    const serializedHeader = JSON.stringify(header.toJSON());
    if (toBase64Url) {
      return base64url.encode(serializedHeader);
    }
    return serializedHeader;
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJweGeneralJSon token
   * @returns true if valid token was parsed
   */
  private setGeneralParts(content: IJweGeneralJson): {result: boolean, reason: string} {
    if (content) {
      if (content.payload) {
        this.payload = content.payload;
      } else {
        // manadatory field
        return {result: false, reason: 'missing payload'};
      }

      this.signatures = content.signatures;
      return this.isValidToken();
    }

    return {result: false, reason: 'no content passed'};
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJweFlatJson token
   * @returns true if valid token was parsed
   */
  private setFlatParts(content: IJweFlatJson): {result: boolean, reason: string} {
    if (content) {
      const signature = new JweRecipient();

      if (content.signature) {
        signature.signature = content.signature;
      } else {
        // manadatory field
        return {result: false, reason: 'missing signature'};
      }

      if (JweToken.headerHasElements(content.protected)) {
        signature.protected = this.setProtected(<JweHeader>content.protected);
      } 

      if (JweToken.headerHasElements(content.header)) {
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
   * @param content Alledged IJweCompact token
   * @returns true if valid token was parsed
   */
  private setCompactParts(content: IJweCompact): {result: boolean, reason: string} {
    if (content) {
      const signature = new JweRecipient();

      if (content.signature) {
        signature.signature = base64url.toBuffer(content.signature);
      } else {
        // manadatory field
        return {result: false, reason: 'missing signature'};
      }

      if (JweToken.headerHasElements(content.protected)) {
        signature.protected = this.setProtected(<JweHeader>content.protected);
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
   * @param protectedHeader to set on the JweToken object
   */
  private setProtected(protectedHeader: string | JweHeader ) {
    if (typeof protectedHeader === 'string' ) {
      const json = base64url.decode(protectedHeader);
      return <JweHeader>JSON.parse(json);
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
    return this.getCryptoFactory(newOptions, manadatory).keyStore;
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
  private getProtected(newOptions?: IKeyStoreOptions, manadatory: boolean = false): JweHeader {
    return this.getOptionsProperty<JweHeader>('protected', newOptions, manadatory);
  }

  /**
   * Get the default header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getHeader(newOptions?: IKeyStoreOptions, manadatory: boolean = false): JweHeader {
    return this.getOptionsProperty<JweHeader>('header', newOptions,manadatory);
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
   * @returns Signed payload in compact Jwe format.
   */
  public async sign (signingKeyReference: string, payload: string, format: ProtectionFormat, options?: ISigningOptions): Promise<JweToken> {
    const keyStore: IKeyStore = this.getKeyStore(options);
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const algorithm: CryptoAlgorithm = this.getAlgorithm(options);
    const alg: string = CryptoHelpers.toJwa(algorithm);

    // tslint:disable-next-line:no-suspicious-comment
    // TODO support for multiple signatures
    const JweRecipient = new JweRecipient();

    // Set payload
    const JweToken = new JweToken();

    // Get signing key public key
    const jwk: PublicKey = await <Promise<PublicKey>>keyStore.get(signingKeyReference, true);

    // Steps according to RTC7515 5.1
    // 2. Compute encoded payload value base64URL(Jwe Payload)
    JweToken.payload = payload;
    const encodedContent = base64url.encode(payload);

    // 3. Compute the headers. 
    JweRecipient.header = this.getHeader(options) || new TSMap<string, string>();
    JweRecipient.protected = this.getProtected(options) || new TSMap<string, string>();

    // Get the required algorithm
    // add required fields if missing
    if (!('alg' in JweRecipient.header) && !('alg' in JweRecipient.protected)) {
      // tslint:disable-next-line:no-backbone-get-set-outside-model
      JweRecipient.protected.set('alg', alg);
    }

    if (jwk.kid && 
      !(JweRecipient.header && 'kid' in JweRecipient.header) && 
      !('kid' in JweRecipient.protected)) {
        // tslint:disable-next-line:no-backbone-get-set-outside-model
        JweRecipient.protected.set('kid', jwk.kid);
    }

    const protectedUsed = JweToken.headerHasElements(JweRecipient.protected);
    
    // 4. Compute BASE64URL(UTF8(Jwe Header))
    const encodedProtected = !protectedUsed ? '' : 
      JweToken.encodeHeader(JweRecipient.protected);

    // 5. Compute the signature using data ASCII(BASE64URL(UTF8(Jwe protected Header))) || . || . BASE64URL(Jwe Payload)
    //    using the "alg" signature algorithm.
    const signatureInput = `${encodedProtected}.${encodedContent}`;

    // call base layer plugable crypto API for signing with a key reference
    const signer = new SubtleCryptoExtension(cryptoFactory);
    const signature = await signer.signByKeyStore(algorithm, signingKeyReference, Buffer.from(signatureInput));
    
    // Compose result
    JweRecipient.signature = Buffer.from(signature);
    JweToken.signatures.push(JweRecipient);
    JweToken.format = format;
    return JweToken;
  }

  /**
   * Gets the base64 URL decrypted payload.
   */
  public getPayload (): string {
    return <string>base64url.decode(<string>this.payload);
  }
}
