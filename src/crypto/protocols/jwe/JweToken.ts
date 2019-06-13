/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoFactory from '../../plugin/CryptoFactory';
import PublicKey from '../../keys/PublicKey';
import IJweGeneralJson, { JweHeader } from './IJweGeneralJson';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import { IJweEncryptionOptions } from "../../protocols/jose/IJoseOptions";
import JweRecipient from './JweRecipient';
import { TSMap } from 'typescript-map'
import JoseHelpers from '../jose/JoseHelpers';
import { KeyType } from '../../keys/KeyTypeFactory';
import JoseConstants from '../jose/JoseConstants'
import CryptoHelpers from '../../utilities/CryptoHelpers';
import SubtleCryptoExtension from '../../plugin/SubtleCryptoExtension';
import base64url from 'base64url';
import IJweRecipient from './IJweRecipient';
import ISubtleCrypto from '../../plugin/ISubtleCrypto';
import CryptoProtocolError from '../CryptoProtocolError';
import IJweFlatJson from './IJweFlatJson';

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
  private options: IJweEncryptionOptions | undefined;

  /**
   * Create an Jwe token object
   * @param options Set of Jwe token options
   */
  constructor (options?: IJweEncryptionOptions) {
    this.options = options;
  }

//#region serialization
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
    
    throw new CryptoProtocolError(JoseConstants.Jwe, `The format '${this.format}' is not supported`);
  }

  /**
   * Serialize a Jwe token object from a token in General Json format
   * @param token Jwe base object
   */
  private static serializeJweGeneralJson (token: JweToken): string {
    let json: any = {
      recipients: [],
      aad: base64url.encode(token.aad),
      iv: base64url.encode(token.iv),
      ciphertext: base64url.encode(token.ciphertext),
      tag: base64url.encode(token.tag)      
    }
    if (JoseHelpers.headerHasElements(token.protected)) {
      json.protected = JoseHelpers.encodeHeader(<JweHeader>token.protected);
    }
    if (JoseHelpers.headerHasElements(token.unprotected)) {
      json.unprotected = JoseHelpers.encodeHeader(<JweHeader>token.unprotected);
    }

    for (let inx = 0 ; inx < token.recipients.length ; inx++ ) {
      const recipient: any = {
        encrypted_key: base64url.encode(token.recipients[inx].encrypted_key)
      }
      if (JoseHelpers.headerHasElements(token.recipients[inx].header)) {
        recipient.header = JoseHelpers.encodeHeader(<JweHeader>token.recipients[inx].header);
      }
      
      json.recipients.push(recipient);
    }

    return JSON.stringify(json);
  }

  /**
   * Serialize a Jwe token object from a token in Flat Json format
   * @param token Jwe base object
   */
  private static serializeJweFlatJson (token: JweToken): string {
    let json: any = {
      encrypted_key: base64url.encode(token.recipients[0].encrypted_key),
      aad: base64url.encode(token.aad),
      iv: base64url.encode(token.iv),
      ciphertext: base64url.encode(token.ciphertext),
      tag: base64url.encode(token.tag)      
    }
    if (JoseHelpers.headerHasElements(token.protected)) {
      json.protected = JoseHelpers.encodeHeader(<JweHeader>token.protected);
    }
    if (JoseHelpers.headerHasElements(token.unprotected)) {
      json.unprotected = JoseHelpers.encodeHeader(<JweHeader>token.unprotected);
    }
    if (JoseHelpers.headerHasElements(token.recipients[0].header)) {
      json.header = JoseHelpers.encodeHeader(<JweHeader>token.recipients[0].header);
    }

    return JSON.stringify(json);
  }

  /**
   * Serialize a Jwe token object from a token in Compact format
   * @param token Jwe base object
   */
  private static serializeJweCompact (token: JweToken): string {
    let encodedProtected = '';
    if (JoseHelpers.headerHasElements(token.protected)) {
      encodedProtected = JoseHelpers.encodeHeader(<JweHeader>token.protected);
    }
    const encryptedKey = base64url.encode(token.recipients[0].encrypted_key);
    const iv = base64url.encode(token.iv);
    const cipher = base64url.encode(token.ciphertext);
    const tag = base64url.encode(token.tag);
    return `${encodedProtected}.${encryptedKey}.${iv}.${cipher}.${tag}`;
  }

  //#endregion

  //#region deserialization
  /**
   * Deserialize a Jwe token object
   */
   public static deserialize (token: string, options?: IJweEncryptionOptions): JweToken {
    const jweToken = new JweToken(options);
      
    // check for JWE compact format
  if (typeof token === 'string') {
    const parts = token.split('.');
    if (parts.length === 5) {
      jweToken.protected = JweToken.setProtected(parts[0]);
      const recipient = new JweRecipient();
      recipient.encrypted_key = base64url.toBuffer(parts[1]);
      jweToken.recipients = [recipient];
      jweToken.ciphertext = base64url.toBuffer(parts[3]);
      jweToken.iv = base64url.toBuffer(parts[2]);
      jweToken.tag = base64url.toBuffer(parts[4]);
      jweToken.aad = base64url.toBuffer(parts[5]);
      return jweToken;
    }
  } else {
    throw new CryptoProtocolError(JoseConstants.Jwe, `The presented object is not deserializable.`);
  }

  // Flat or general format
  let jsonObject: any;
  try {
    jsonObject = JSON.parse(token);
  } catch (error) {
    throw new CryptoProtocolError(JoseConstants.Jwe, `The presented object is not deserializable and is no compact format.`);
  }

  // Try to handle token as IJweGeneralJSon
  let decodeStatus: { result: boolean, reason: string } = jweToken.setGeneralParts(<IJweGeneralJson>jsonObject);
     if (decodeStatus.result) {
         return jweToken;
     } else {
       console.debug(`Failed parsing as IJweGeneralJSon. Reason: ${decodeStatus.reason}`)
     }

     // Try to handle token as IJweFlatJson
     decodeStatus = jweToken.setFlatParts(<IJweFlatJson>jsonObject);
     if (decodeStatus.result) {
         return jweToken;
     } else {
       console.debug(`Failed parsing as IJweFlatJson. Reason: ${decodeStatus.reason}`);
     }
   throw new CryptoProtocolError(JoseConstants.Jwe, `The content does not represent a valid jwe token`);  
  }

  /**
   * Try to parse the input token and set the properties of this JswToken
   * @param content Alledged IJweGeneralJSon token
   * @returns true if valid token was parsed
   */
  private setGeneralParts(content: IJweGeneralJson): {result: boolean, reason: string} {
    if (content) {
      if (content.recipients) {
        this.ciphertext = base64url.toBuffer(<string><any>content.ciphertext);
        this.aad = base64url.toBuffer(<string><any>content.aad);
        this.iv = base64url.toBuffer(<string><any>content.iv);
        this.protected = JweToken.setProtected(content.protected);
        this.tag = base64url.toBuffer(<string><any>content.tag);
        this.recipients = [];
        for (let inx = 0; inx < content.recipients.length; inx ++) {
          const recipient = new JweRecipient();
          recipient.encrypted_key = base64url.toBuffer(<string><any>this.recipients[inx].encrypted_key);          
          recipient.header = this.recipients[inx].header;          
        }
      } else {
        // manadatory field
        return {result: false, reason: 'missing recipients'};
      }

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
      const recipient = new JweRecipient();

      if (content.ciphertext) {
        this.ciphertext = content.ciphertext;
      } else {
        // manadatory field
        return {result: false, reason: 'missing ciphertext'};
      }

      if (content.encrypted_key) {
        recipient.encrypted_key = content.encrypted_key;
      } else {
        // manadatory field
        return {result: false, reason: 'missing encrypted_key'};
      }

      if (content.iv) {
        this.iv = content.iv;
      } else {
        // manadatory field
        return {result: false, reason: 'missing iv'};
      }

      if (JoseHelpers.headerHasElements(content.protected)) {
        this.protected = JweToken.setProtected(<JweHeader>content.protected);
      } 

      if (JoseHelpers.headerHasElements(content.unprotected)) {
        this.unprotected = content.unprotected;
      } 

      if (JoseHelpers.headerHasElements(content.header)) {
        recipient.header = content.header;
      } 

      this.recipients = [recipient];
      return this.isValidToken();
    } 

    return {result: false, reason: 'no content passed'};
  }

  /**
   * Check if a valid token was found after decoding
   */
  private isValidToken(): {result: boolean, reason: string} {
    if (!this.ciphertext) {
      return {result: false, reason: 'missing ciphertext'};
    }

    if (!this.iv) {
      return {result: false, reason: 'missing iv'};
    }

    if (!this.recipients) {
      return {result: false, reason: 'missing recipients'};
    }

    if (this.recipients.length == 0) {
      return {result: false, reason: 'recipients array is empty'};
    }

    for (let inx = 0; inx < this.recipients.length; inx++) {
      const recipient = this.recipients[inx];
      if (!recipient.encrypted_key) {
        return {result: false, reason: `recipient ${inx} is missing encrypted_key`};
      }
      if (!this.protected && !recipient.header) {
        return {result: false, reason: `recipient ${inx} is missing header and protected is also missing`};
      }
    }

    return {result: true, reason: ''};
  }
//#endregion
//#region options section
  /**
   * Get the CryptoFactory to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getCryptoFactory(newOptions?: IJweEncryptionOptions, manadatory: boolean = true): CryptoFactory {
    return JoseHelpers.getOptionsProperty<CryptoFactory>('cryptoFactory', this.options, newOptions, manadatory);
  }

  /**
   * Get the key encryption key for testing
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getContentEncryptionKey(newOptions?: IJweEncryptionOptions, manadatory: boolean = true): Buffer {
    return JoseHelpers.getOptionsProperty<Buffer>('contentEncryptionKey', this.options, newOptions, manadatory);
  }

  /**
   * Get the initial vector for testing
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getInitialVector(newOptions?: IJweEncryptionOptions, manadatory: boolean = true): Buffer {
    return JoseHelpers.getOptionsProperty<Buffer>('initialVector', this.options, newOptions, manadatory);
  }

  /**
   * Get the content encryption algorithm from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getContentEncryptionAlgorithm(newOptions?: IJweEncryptionOptions, manadatory: boolean = true): string {
    return JoseHelpers.getOptionsProperty<string>('contentEncryptionAlgorithm', this.options, newOptions, manadatory);
  }
//#endregion
//#region encryption functions
  /**
   * Encrypt content using the given public keys in JWK format.
   * The key type enforces the key encryption algorithm.
   * The options can override certain algorithm choices.
   * 
   * @param recipients List of recipients' public keys.
   * @param payload to encrypt.
   * @param format of the final serialization.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns JweToken with encrypted payload.
   */
  public async encrypt (recipients: PublicKey[], payload: string, format: ProtectionFormat, options?: IJweEncryptionOptions): Promise<JweToken> {
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const contentEncryptionAlgorithm = this.getContentEncryptionAlgorithm(options);
    this.format = format;

    // encoded protected header
    let encodedProtected: string = '';

    // Set the resulting token
    const jweToken: JweToken = new JweToken(options || this.options);

    // Get the encryptor extensions
    const encryptor = new SubtleCryptoExtension(cryptoFactory);

    // Set the content encryption key
    let randomGenerator = CryptoHelpers.jwaToWebCrypto(contentEncryptionAlgorithm);
    let contentEncryptionKey: Buffer = this.getContentEncryptionKey(options, false);
    if (!contentEncryptionKey) {
      const key = await encryptor.generateKey(randomGenerator, true, ['encrypt']);
      const jwk: any = await encryptor.exportKey('jwk', <CryptoKey>key);
      contentEncryptionKey = base64url.toBuffer(jwk.k);
    }
    
      // Set the initial vector
      jweToken.iv = this.getInitialVector(options, false);
      if (!jweToken.iv) {
        const key = await encryptor.generateKey(randomGenerator, true, ['encrypt']);
        const jwk: any = await encryptor.exportKey('jwk', <CryptoKey>key);
        jweToken.iv = base64url.toBuffer(jwk.k);
      }

      // Needs to be improved when alg is not provided.
      // Decide key encryption algorithm based on given JWK.
      let publicKey: PublicKey = recipients[0];
      for (let key of recipients) {
        if (key.alg === JoseConstants.RsaOaep256) {
          publicKey = key;
          break;
        }
      }
      let keyEncryptionAlgorithm: string  | undefined = publicKey.alg;
      if (!keyEncryptionAlgorithm) {
        if (publicKey.kty == KeyType.EC) {
          throw new Error('EC encryption not implemented');
        } else {
          // Default RSA algorithm
          keyEncryptionAlgorithm = JoseConstants.RsaOaep256;
        }
      }
    
        // tslint:disable: no-backbone-get-set-outside-model
        jweToken.protected.set(JoseConstants.Alg, <string>keyEncryptionAlgorithm);
        jweToken.protected.set(JoseConstants.Enc, <string>contentEncryptionAlgorithm);
    
        if (publicKey.kid) {
          jweToken.protected.set(JoseConstants.Kid, publicKey.kid);
        }
        
        encodedProtected = JoseHelpers.headerHasElements(jweToken.protected) ?  
          JoseHelpers.encodeHeader(jweToken.protected) :
          '';

      // Set aad as the protected header
      jweToken.aad = base64url.toBuffer(encodedProtected);

    for (let inx = 0 ; inx < recipients.length; inx ++) {
      // Set the recipients structure
      const jweRecipient = new JweRecipient();
      jweToken.recipients.push(jweRecipient);

      // Decide key encryption algorithm based on given JWK.
      publicKey = recipients[inx];

      // Get key encrypter and encrypt the content key
      jweRecipient.encrypted_key = Buffer.from(
        await encryptor.encryptByJwk(
          CryptoHelpers.jwaToWebCrypto(<string>keyEncryptionAlgorithm),
          publicKey,
          contentEncryptionKey));
    }

    // encrypt content
    const contentEncryptorKey: JsonWebKey = {
      k: base64url.encode(contentEncryptionKey),
      alg: contentEncryptionAlgorithm,
      kty: 'oct'
    };

    const encodedAad = base64url.encode(jweToken.aad);
    const cipherText = await encryptor.encryptByJwk(
      CryptoHelpers.jwaToWebCrypto(contentEncryptionAlgorithm, new Uint8Array(jweToken.iv), new Uint8Array(Buffer.from(encodedAad))),
      contentEncryptorKey,
      Buffer.from(payload));

    jweToken.tag = Buffer.from(cipherText, cipherText.byteLength - 16);
    jweToken.ciphertext = Buffer.from(cipherText, 0 , cipherText.byteLength - 16);
      
    return jweToken;
  }
  //#endregion

  //#region decryption functions
  /**
   * Decrypt the content.
   * 
   * @param decryptionKeyReference Reference to the decryption key.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in compact Jwe format.
   */
// tslint:disable-next-line: max-func-body-length
   public async decrypt (decryptionKeyReference: string, options?: IJweEncryptionOptions): Promise<Buffer> {
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);

    // Get the encryptor extensions
    const decryptor = new SubtleCryptoExtension(cryptoFactory);

    // get decryption public key
    let jwk: PublicKey = await <Promise<PublicKey>>cryptoFactory.keyStore.get(decryptionKeyReference, true);

    // Get the encrypted key
    // Check if kid matches
    let contentEncryptionKey: Buffer | undefined;
    if (jwk.kid) {
    for (let inx = 0 ; inx < this.recipients.length ; inx ++) {
      const recipient = this.recipients[inx];
      if (recipient.header) {
        const headerKid = recipient.header.get(JoseConstants.Kid); 
        if (headerKid && headerKid === jwk.kid) {
          if (contentEncryptionKey = await this.decryptContentEncryptionKey(recipient, decryptor, decryptionKeyReference)) {
            break;
          }
        }
      }
    }
  }

  if (!contentEncryptionKey) {
    // try to decrypt every key
    for (let inx = 0, length = this.recipients.length ; inx < length ; inx ++) {
      const recipient = this.recipients[inx];
      if (contentEncryptionKey = await this.decryptContentEncryptionKey(recipient, decryptor, decryptionKeyReference)) {
        break;
      }
    }
  }

  if (!contentEncryptionKey) {
    throw new CryptoProtocolError(JoseConstants.Jwe, 'Cannot decrypt the content encryption key because of missing key');
  }

  // Decrypt content
  const contentEncryptionAlgorithm = this.protected.get(JoseConstants.Enc);
  const iv =  new Uint8Array(this.iv); 
  const encodedAad = base64url.encode(this.aad);
  const aad = new Uint8Array(Buffer.from(encodedAad));
  const algorithm = CryptoHelpers.jwaToWebCrypto(contentEncryptionAlgorithm, iv, aad);    
  const contentJwk: JsonWebKey = {
    kty: 'oct',
    alg: contentEncryptionAlgorithm,
    k: base64url.encode(contentEncryptionKey)
  };

  const plaintext =  await decryptor.decryptByJwk(algorithm, contentJwk, Buffer.concat([this.ciphertext, this.tag]));
  return Buffer.from(plaintext);
  }

  private async decryptContentEncryptionKey(recipient: IJweRecipient, decryptor: ISubtleCrypto, decryptionKeyReference: string): Promise<Buffer> {
    let keyDecryptionAlgorithm = '';
    if (!recipient.header) {
      keyDecryptionAlgorithm = this.protected.get(JoseConstants.Alg);
    } else {
      keyDecryptionAlgorithm = recipient.header.get(JoseConstants.Alg) || this.protected.get(JoseConstants.Alg);
    }

    const algorithm = CryptoHelpers.jwaToWebCrypto(keyDecryptionAlgorithm);    
    return Buffer.from(await decryptor.decryptByKeyStore(algorithm, decryptionKeyReference, recipient.encrypted_key));
   }
  //#endregion
  
  /**
   * Set the protected header
   * @param protectedHeader to set on the JwsToken object
   */
  private static setProtected(protectedHeader: string | JweHeader) {
    if (typeof protectedHeader === 'string') {
      const json = base64url.decode(protectedHeader);
      return <JweHeader>JSON.parse(json);
    }

    return protectedHeader;
  }

}
