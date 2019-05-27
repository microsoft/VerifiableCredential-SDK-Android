/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoFactory from '../../plugin/CryptoFactory';
import PublicKey from '../../keys/PublicKey';
import IJweGeneralJson, { JweHeader } from './IJweGeneralJson';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import { IEncryptionOptions } from '../../keyStore/IKeyStore';
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
  private options: IEncryptionOptions | undefined;

  /**
   * Create an Jwe token object
   * @param options Set of Jwe token options
   */
  constructor (options?: IEncryptionOptions) {
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
//#region options section
  /**
   * Get the CryptoFactory to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getCryptoFactory(newOptions?: IEncryptionOptions, manadatory: boolean = true): CryptoFactory {
    return JoseHelpers.getOptionsProperty<CryptoFactory>('cryptoFactory', this.options, newOptions, manadatory);
  }

  /**
   * Get the key encryption key for testing
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getContentEncryptionKey(newOptions?: IEncryptionOptions, manadatory: boolean = true): Buffer {
    return JoseHelpers.getOptionsProperty<Buffer>('contentEncryptionKey', this.options, newOptions, manadatory);
  }

  /**
   * Get the initial vector for testing
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getInitialVector(newOptions?: IEncryptionOptions, manadatory: boolean = true): Buffer {
    return JoseHelpers.getOptionsProperty<Buffer>('initialVector', this.options, newOptions, manadatory);
  }

  /**
   * Get the content encryption algorithm from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getContentEncryptionAlgorithm(newOptions?: IEncryptionOptions, manadatory: boolean = true): string {
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
  public async encrypt (recipients: PublicKey[], payload: string, format: ProtectionFormat, options?: IEncryptionOptions): Promise<JweToken> {
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const contentEncryptionAlgorithm = this.getContentEncryptionAlgorithm(options);
    this.format = format;

    // encoded protected header
    let encodedProtected: string = '';

    // Set the resulting token
    const jweToken: JweToken = new JweToken(options || this.options);

    // Set the content encryption key
    const contentEncryptionKey: Buffer = this.getContentEncryptionKey(options);

    // Get the encryptor extensions
    const encryptor = new SubtleCryptoExtension(cryptoFactory);
    
      // Set the initial vector
      jweToken.iv = this.getInitialVector(options);

      // Needs to be improved when alg is not provided.
      // Decide key encryption algorithm based on given JWK.
      let publicKey: PublicKey = recipients[0];
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
        jweToken.protected.set('alg', <string>keyEncryptionAlgorithm);
        jweToken.protected.set('enc', <string>contentEncryptionAlgorithm);
    
        if (publicKey.kid) {
          jweToken.protected.set('kid', publicKey.kid);
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
   public async decrypt (decryptionKeyReference: string, options?: IEncryptionOptions): Promise<Buffer> {
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
        const headerKid = recipient.header.get('kid'); 
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
  const contentEncryptionAlgorithm = this.protected.get('enc');
  const iv =  new Uint8Array(this.iv); 
  const encodedAad = base64url.encode(this.aad);
  const aad = new Uint8Array(Buffer.from(encodedAad));
  const algorithm = CryptoHelpers.jwaToWebCrypto(contentEncryptionAlgorithm, iv, aad);    
  const contentJwk: JsonWebKey = {
    kty: 'oct',
    k: base64url.encode(contentEncryptionKey)
  };

  const plaintext =  await decryptor.decryptByJwk(algorithm, contentJwk, Buffer.concat([this.ciphertext, this.tag]));
  return Buffer.from(plaintext);
  }

  private async decryptContentEncryptionKey(recipient: IJweRecipient, decryptor: ISubtleCrypto, decryptionKeyReference: string): Promise<Buffer> {
    let keyDecryptionAlgorithm = '';
    if (!recipient.header) {
      keyDecryptionAlgorithm = this.protected.get('alg');
    } else {
      keyDecryptionAlgorithm = recipient.header.get('alg') || this.protected.get('alg');
    }

    const algorithm = CryptoHelpers.jwaToWebCrypto(keyDecryptionAlgorithm);    
    return Buffer.from(await decryptor.decryptByKeyStore(algorithm, decryptionKeyReference, recipient.encrypted_key));
   }
  //#endregion
}
