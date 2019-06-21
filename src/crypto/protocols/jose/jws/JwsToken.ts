/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import CryptoFactory from '../../../plugin/CryptoFactory';
import PublicKey from '../../../keys/PublicKey';
import IJwsFlatJson from './IJwsFlatJson';
import IJwsGeneralJson, { JwsHeader } from './IJwsGeneralJson';
import { ProtectionFormat } from '../../../keyStore/ProtectionFormat';
import { IJwsSigningOptions } from '../IJoseOptions';
import IKeyStore, { CryptoAlgorithm } from '../../../keyStore/IKeyStore';
import CryptoHelpers from '../../../utilities/CryptoHelpers';
import SubtleCryptoExtension from '../../../plugin/SubtleCryptoExtension';
import JwsSignature from './JwsSignature';
import { TSMap } from 'typescript-map';
import JoseHelpers from '../JoseHelpers';
import IJwsSignature from './IJwsSignature';
import ISubtleCrypto from '../../../plugin/ISubtleCrypto';
import JoseConstants from '../JoseConstants';
import CryptoProtocolError from '../../CryptoProtocolError';
import { ICryptoToken } from '../../ICryptoToken';
import IPayloadProtectionProtocolOptions from '../../IPayloadProtectionProtocolOptions';
import JoseProtocol from '../JoseProtocol';
import { stringify } from 'querystring';

/**
 * Class for containing JWS token operations.
 * This class hides the JOSE and crypto library dependencies to allow support for additional crypto algorithms.
 * Crypto calls always happen via CryptoFactory
 */
export default class JwsToken implements IJwsGeneralJson {
  /**
   * Payload (base64url encoded)
   */
  public payload: Buffer = Buffer.from('');

  /**
   * Signatures on content
   */
  public signatures: JwsSignature[] = [];

  /**
   * Get the request serialization format
   */
  public format: ProtectionFormat = ProtectionFormat.JweGeneralJson;

  // Options passed into the constructor
  private options: IJwsSigningOptions | undefined;

  /**
   * Create an Jws token object
   * @param options Set of jws token options
   */
  constructor(options?: IJwsSigningOptions) {
    this.options = options;
  }

  //#region serialization
  /**
   * Serialize a Jws token object from a token
   * @param format Optional specify the serialization format. If not specified, use default format.
   */
  public serialize(format?: ProtectionFormat): string {
    if (format === undefined) {
      format = this.format;
    }

    switch (format) {
      case ProtectionFormat.JwsGeneralJson:
        return JwsToken.serializeJwsGeneralJson(this);
      case ProtectionFormat.JwsCompactJson:
        return JwsToken.serializeJwsCompact(this);
      case ProtectionFormat.JwsFlatJson:
        return JwsToken.serializeJwsFlatJson(this);
    }

    throw new CryptoProtocolError(JoseConstants.Jws, `The format '${this.format}' is not supported`);
  }

  /**
   * Serialize a Jws token object from a token in General Json format
   * @param token JWS base object
   */
  private static serializeJwsGeneralJson(token: JwsToken): string {
    const jws = {
      payload: base64url.encode(token.payload),
      signatures: <any[]>[]
    };

    for (let inx = 0; inx < token.signatures.length; inx++) {
      const tokenSignature: JwsSignature = token.signatures[inx];
      const jwsSignature: any = {
        signature: base64url.encode(tokenSignature.signature)
      };
      if (JoseHelpers.headerHasElements(tokenSignature.protected)) {
        jwsSignature.protected = JoseHelpers.encodeHeader(<JwsHeader>tokenSignature.protected);
      }
      if (JoseHelpers.headerHasElements(tokenSignature.header)) {
        jwsSignature.header = JoseHelpers.encodeHeader(<JwsHeader>tokenSignature.header, false);
      }
      if (!jwsSignature.protected && !jwsSignature.header) {
        throw new CryptoProtocolError(JoseConstants.Jws, `Signature ${inx} is missing header and protected`);
      }
      jws.signatures.push(jwsSignature);
    }

    return JSON.stringify(jws);
  }

  /**
   * Serialize a Jws token object from a token in Flat Json format
   * @param token JWS base object
   */
  private static serializeJwsFlatJson(token: JwsToken): string {
    const jws: any = {
      payload: base64url.encode(token.payload)
    };

    if (JoseHelpers.headerHasElements(token.signatures[0].protected)) {
      jws.protected = JoseHelpers.encodeHeader(<JwsHeader>token.signatures[0].protected);
    }
    if (JoseHelpers.headerHasElements(token.signatures[0].header)) {
      jws.header = <JwsHeader>token.signatures[0].header;
    }
    jws.signature = base64url.encode(token.signatures[0].signature);
    return JSON.stringify(jws);
  }

  /**
   * Serialize a Jws token object from a token in Compact format
   * @param token JWS base object
   */
  private static serializeJwsCompact(token: JwsToken): string {
    let encodedProtected: string = '';
    if (JoseHelpers.headerHasElements(token.signatures[0].protected)) {
      encodedProtected = JoseHelpers.encodeHeader(<JwsHeader>token.signatures[0].protected);
    }

    const encodedpayload = base64url.encode(token.payload);
    const encodedSignature = base64url.encode(token.signatures[0].signature);
    return `${encodedProtected}.${encodedpayload}.${encodedSignature}`;
  }
  //#endregion

  //#region deserialization
  /**
   * Deserialize a Jws token object
   */
  public static deserialize(token: string, options?: IJwsSigningOptions): JwsToken {
    const jwsToken = new JwsToken(options);

    // check for JWS compact format
    if (typeof token === 'string') {
      const parts = token.split('.');
      if (parts.length === 3) {
        jwsToken.payload = base64url.toBuffer(parts[1]);
        const signature = new JwsSignature();
        signature.protected = jwsToken.setProtected(parts[0]);
        signature.signature = base64url.toBuffer(parts[2]);
        jwsToken.signatures = [signature];
        return jwsToken;
      }
    } else {
      throw new CryptoProtocolError(JoseConstants.Jws, `The presented object is not deserializable.`);
    }

    // Flat or general format
    let jsonObject: any;
    try {
      jsonObject = JSON.parse(token);
    } catch (error) {
      throw new CryptoProtocolError(
        JoseConstants.Jws,
        `The presented object is not deserializable and is no compact format.`
      );
    }

    // set payload
    jwsToken.payload = base64url.toBuffer(<string>jsonObject.payload);

    // Try to handle token as IJwsGeneralJSon
    let decodeStatus: { result: boolean; reason: string } = jwsToken.setGeneralParts(<IJwsGeneralJson>jsonObject);
    if (decodeStatus.result) {
      return jwsToken;
    } else {
      console.debug(`Failed parsing as IJwsGeneralJSon. Reason: ${decodeStatus.reason}`);
    }

    // Try to handle token as IJwsFlatJson
    decodeStatus = jwsToken.setFlatParts(<IJwsFlatJson>jsonObject);
    if (decodeStatus.result) {
      return jwsToken;
    } else {
      console.debug(`Failed parsing as IJwsFlatJson. Reason: ${decodeStatus.reason}`);
    }

    // If this point is reached we have not been passed a usable JWS token. 
    throw new CryptoProtocolError(JoseConstants.Jws, 'The provided token is not a valid JWS token.');
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJwsGeneralJSon token
   * @returns true if valid token was parsed
   */
  private setGeneralParts(content: IJwsGeneralJson): { result: boolean; reason: string } {
    if (content) {
      if (content.payload) {
        this.payload = base64url.toBuffer(<string>(<any>content.payload));
      } else {
        // manadatory field
        return { result: false, reason: 'missing payload' };
      }

      if (!content.signatures) {
       // manadatory field
        return { result: false, reason: 'missing signatures' };
      }

      this.signatures = [];
      for (let inx = 0 ; inx < content.signatures.length ; inx ++) {
        const jwsSignature = new JwsSignature();
        jwsSignature.signature = base64url.toBuffer((<any>content).signatures[inx].signature);
        if (content.signatures[inx].header) {
          jwsSignature.header = this.setHeader((<any>content).signatures[inx].header);
        }
        if (content.signatures[inx].protected) {
          jwsSignature.protected = this.setProtected((<any>content).signatures[inx].protected);
        }
        this.signatures.push(jwsSignature);
      }

      return this.isValidToken();
    }

    return { result: false, reason: 'no content passed' };
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJwsFlatJson token
   * @returns true if valid token was parsed
   */
  private setFlatParts(content: IJwsFlatJson): { result: boolean; reason: string } {
    if (content) {
      const signature = new JwsSignature();

      if (content.signature) {
        signature.signature = base64url.toBuffer(<any>content.signature);
      } else {
        // manadatory field
        return { result: false, reason: 'missing signature' };
      }

      if (JoseHelpers.headerHasElements(content.protected)) {
        signature.protected = this.setProtected(<JwsHeader>content.protected);
      }

      if (JoseHelpers.headerHasElements(content.header)) {
        signature.header = this.setHeader(JSON.stringify(content.header));
      }

      if (content.payload) {
        this.payload = base64url.toBuffer(<string>(<any>content.payload));
      } else {
        // manadatory field
        return { result: false, reason: 'missing payload' };
      }

      this.signatures = [signature];
      return this.isValidToken();
    }

    return { result: false, reason: 'no content passed' };
  }

  /**
   * Check if a valid token was found after decoding
   */
  private isValidToken(): { result: boolean; reason: string } {
    if (!this.payload) {
      return { result: false, reason: 'missing payload' };
    }

    if (!this.signatures) {
      return { result: false, reason: 'missing signatures' };
    }

    const noOfSignatures = this.signatures.length;
    if (noOfSignatures === 0) {
      return { result: false, reason: 'signatures array is empty' };
    }

    for (let inx = 0; inx < noOfSignatures; inx++) {
      const signature = this.signatures[inx];
      if (!signature.signature) {
        return { result: false, reason: `signature ${inx} is missing signature` };
      }
      if (!signature.header && !signature.protected) {
        return { result: false, reason: `signature ${inx} is missing header and protected` };
      }
    }

    return { result: true, reason: '' };
  }
  //#endregion

  /**
   * Get the keyStore to be used
   * @param newOptions Options passed in after the constructure
   * @param mandatory True if property needs to be defined
   */
  private getKeyStore(newOptions?: IJwsSigningOptions, mandatory: boolean = true): IKeyStore {
    return this.getCryptoFactory(newOptions, mandatory).keyStore;
  }

  /**
   * Get the CryptoFactory to be used
   * @param newOptions Options passed in after the constructure
   * @param mandatory True if property needs to be defined
   */
  private getCryptoFactory(newOptions?: IJwsSigningOptions, mandatory: boolean = true): CryptoFactory {
    return JoseHelpers.getOptionsProperty<CryptoFactory>('cryptoFactory', this.options, newOptions, mandatory);
  }

  /**
   * Get the default protected header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param mandatory True if property needs to be defined
   */
  private getProtected(newOptions?: IJwsSigningOptions, mandatory: boolean = false): JwsHeader {
    return JoseHelpers.getOptionsProperty<JwsHeader>('protected', this.options, newOptions, mandatory);
  }

  /**
   * Get the default header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param mandatory True if property needs to be defined
   */
  public getHeader(newOptions?: IJwsSigningOptions, mandatory: boolean = false): JwsHeader {
    return JoseHelpers.getOptionsProperty<JwsHeader>('header', this.options, newOptions, mandatory);
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
  public async sign(
    signingKeyReference: string,
    payload: Buffer,
    format: ProtectionFormat,
    options?: IJwsSigningOptions
  ): Promise<JwsToken> {
    const keyStore: IKeyStore = this.getKeyStore(options);
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);

    // tslint:disable-next-line:no-suspicious-comment
    // TODO support for multiple signatures
    const jwsSignature = new JwsSignature();

    // Set payload
    const jwsToken = new JwsToken(this.options);

    // Get signing key public key
    const jwk: PublicKey = await (<Promise<PublicKey>>keyStore.get(signingKeyReference, true));
    const jwaAlgorithm: string = jwk.alg || JoseConstants.DefaultSigningAlgorithm;
    const algorithm: CryptoAlgorithm = CryptoHelpers.jwaToWebCrypto(jwaAlgorithm);

    // Steps according to RTC7515 5.1
    // 2. Compute encoded payload value base64URL(JWS Payload)
    jwsToken.payload = payload;
    const encodedContent = base64url.encode(payload);

    // 3. Compute the headers.
    jwsSignature.header = this.getHeader(options) || new TSMap<string, string>();
    jwsSignature.protected = this.getProtected(options) || new TSMap<string, string>();

    // Check if header specifies certain constants
    // If defined with no value, the value will be placed in.
    const algInHeader = jwsSignature.header.has(JoseConstants.Alg);
    if (algInHeader) {
      if (!jwsSignature.header.get(JoseConstants.Alg)) {
        jwsSignature.header.set(JoseConstants.Alg, jwaAlgorithm);
      }
    } else {
      jwsSignature.protected.set(JoseConstants.Alg, jwaAlgorithm);
    }
    const kidInHeader = jwsSignature.header.has(JoseConstants.Kid);
    if (kidInHeader) {
      if (!jwsSignature.header.get(JoseConstants.Kid)) {
        if (jwk.kid) {
          jwsSignature.header.set(JoseConstants.Kid, jwk.kid);
        } else {
          jwsSignature.header.delete(JoseConstants.Kid);
        }
      }
    } else {
      if (jwk.kid) {
        jwsSignature.header.set(JoseConstants.Kid, jwk.kid);
      }
    }

    const protectedUsed = JoseHelpers.headerHasElements(jwsSignature.protected);

    // 4. Compute BASE64URL(UTF8(JWS Header))
    const encodedProtected = !protectedUsed ? '' : JoseHelpers.encodeHeader(jwsSignature.protected);

    // 5. Compute the signature using data ASCII(BASE64URL(UTF8(JWS protected Header))) || . || . BASE64URL(JWS Payload)
    //    using the "alg" signature algorithm.
    const signatureInput = `${encodedProtected}.${encodedContent}`;

    // call base layer plugable crypto API for signing with a key reference
    const signer = new SubtleCryptoExtension(cryptoFactory);
    const signature = await signer.signByKeyStore(algorithm, signingKeyReference, Buffer.from(signatureInput));

    // Compose result
    jwsSignature.signature = Buffer.from(signature);
    jwsToken.signatures.push(jwsSignature);
    jwsToken.format = format;
    return jwsToken;
  }

  /**
   * Verify the JWS signature.
   *
   * @param validationKeys Public JWK key to validate the signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns True if signature validated.
   */
   public async verify (validationKeys: PublicKey[], options?: IJwsSigningOptions): Promise<boolean> {
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const validator = new SubtleCryptoExtension(cryptoFactory);

    // Get the encrypted key
    // Check if kid matches
    let success: boolean | undefined;
    for (let inx = 0; inx < this.signatures.length; inx++) {
      const payloadSignature = this.signatures[inx];
      // We need to support an array of public keys todo
      if ((success = await this.validate(payloadSignature, validator, validationKeys[0]))) {
        if (success) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Gets the base64 URL decrypted payload.
   */
  public getPayload(): string {
    return this.payload.toString('utf8');
  }

  /**
   * Convert a @class ICryptoToken into a @class JwsToken
   * @param cryptoToken to convert
   * @param protectOptions options for the token
   */
   public static fromCryptoToken(cryptoToken: ICryptoToken, protectOptions: IPayloadProtectionProtocolOptions): JwsToken {
    const options = JwsToken.fromPayloadProtectionOptions(protectOptions);
    const jwsToken = new JwsToken(options);
    jwsToken.payload = <Buffer>cryptoToken.get(JoseConstants.tokenPayload);
    jwsToken.format = <ProtectionFormat>cryptoToken.get(JoseConstants.tokenFormat);
    jwsToken.signatures =  <JwsSignature[]>cryptoToken.get(JoseConstants.tokenSignatures);
    return jwsToken;
  }

  /**
   * Convert a @class JwsToken into a @class ICryptoToken
   * @param protocolFormat format of the token
   * @param jwsToken to convert
   */
   public static toCryptoToken(protocolFormat: ProtectionFormat, jwsToken: JwsToken): ICryptoToken {
    const cryptoToken = new TSMap<string, any>();
    cryptoToken.set(JoseConstants.tokenPayload, jwsToken.payload);
    cryptoToken.set(JoseConstants.tokenSignatures, jwsToken.signatures);
    cryptoToken.set(JoseConstants.tokenFormat, protocolFormat);
    return cryptoToken;
  }

  /**
   * Convert a @class IPayloadProtectionProtocolOptions into a @class IJwsSigningOptions
   * @param protectOptions to convert
   */
   public static fromPayloadProtectionOptions(protectOptions: IPayloadProtectionProtocolOptions): IJwsSigningOptions {
    return <IJwsSigningOptions>{
      cryptoFactory: protectOptions.cryptoFactory,
      protected: protectOptions.protocolOption.has(JoseConstants.optionProtectedHeader) ? <JwsHeader>protectOptions.protocolOption.get(JoseConstants.optionProtectedHeader) : undefined, 
      header: protectOptions.protocolOption.has(JoseConstants.optionHeader) ? <JwsHeader>protectOptions.protocolOption.get(JoseConstants.optionHeader) : undefined,
      kidPrefix:  protectOptions.protocolOption.has(JoseConstants.optionKidPrefix) ? <JwsHeader>protectOptions.protocolOption.get(JoseConstants.optionKidPrefix) : undefined
    };
  }

  /**
   * Convert a @class IPayloadProtectionProtocolOptions into a @class IJwsSigningOptions
   * @param signingOptions to convert
   */
   public static toPayloadProtectionOptions(signingOptions: IJwsSigningOptions): IPayloadProtectionProtocolOptions {
    const protectOptions = {
      cryptoFactory: signingOptions.cryptoFactory,
      protocolInterface: new JoseProtocol(),
      protocolOption: new TSMap<string, any>() 
    };
    if (signingOptions.header) {
      protectOptions.protocolOption.set(JoseConstants.optionHeader, signingOptions.header);
    }
    if (signingOptions.protected) {
      protectOptions.protocolOption.set(JoseConstants.optionProtectedHeader, signingOptions.protected);
    }
    if (signingOptions.kidPrefix) {
      protectOptions.protocolOption.set(JoseConstants.optionKidPrefix, signingOptions.kidPrefix);
    }
    
    return protectOptions;
  }

  // Validate the current state for completeness
  private async validate(
    payloadSignature: IJwsSignature,
    validator: ISubtleCrypto,
    validationKey: PublicKey
  ): Promise<boolean> {
    let alg: string | undefined;
    const protectedHeader = payloadSignature.protected;
    if (protectedHeader) {
      // tslint:disable-next-line: no-backbone-get-set-outside-model
      alg = protectedHeader.get(JoseConstants.Alg);
    }

    const header = payloadSignature.header;
    if (!alg) {
      if (header) {
        // tslint:disable-next-line: no-backbone-get-set-outside-model
        alg = header.get(JoseConstants.Alg);
      }
    }

    if (!alg) {
      throw new CryptoProtocolError(
        JoseConstants.Jws,
        'Unable to validate signature as no signature algorithm has been specified in the header.'
      );
    }
    const algorithm = CryptoHelpers.jwaToWebCrypto(alg);
    const encodedProtected = !protectedHeader ? '' : JoseHelpers.encodeHeader(protectedHeader);
    const encodedContent = base64url.encode(this.payload);
    const signatureInput = `${encodedProtected}.${encodedContent}`;

    return validator.verifyByJwk(algorithm, validationKey, payloadSignature.signature, Buffer.from(signatureInput));
  }

  /**
   * Set the protected header
   * @param protectedHeader to set on the JwsToken object
   */
   private setProtected(protectedHeader: string | JwsHeader) {
    if (typeof protectedHeader === 'string') {
      const json = base64url.decode(protectedHeader);
      return new TSMap<string, string>().fromJSON(JSON.parse(json));
    }

    return protectedHeader;
  }

  /**
   * Set the header for the signature
   * @param header to set on the JwsToken object
   */
   private setHeader(header: string ) {
    return new TSMap<string, string>().fromJSON(JSON.parse(header));
  }
}
