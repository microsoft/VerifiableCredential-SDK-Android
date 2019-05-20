/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CryptoFactory from '../../plugin/CryptoFactory';
import PublicKey from '../../keys/PublicKey';
import IJweGeneralJson, { JweHeader } from './IJweGeneralJson';
import { ProtectionFormat } from '../../keyStore/ProtectionFormat';
import { IEncryptionOptions } from '../../keystore/IKeyStore';
//import CryptoHelpers from '../../utilities/CryptoHelpers';
//import SubtleCryptoExtension from '../../plugin/SubtleCryptoExtension';
import JweRecipient from './JweRecipient';
import { TSMap } from 'typescript-map'
import JoseHelpers from '../jose/JoseHelpers';
import { KeyType } from '../../keys/KeyType';
import JoseConstants from '../jose/JoseConstants'
import CryptoHelpers from '../../utilities/CryptoHelpers';
import SubtleCryptoExtension from '../../plugin/SubtleCryptoExtension';
import base64url from 'base64url';

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
    
    throw new Error(`The format '${this.format}' is not supported`);
  }

  /**
   * Serialize a Jwe token object from a token in General Json format
   * @param token Jwe base object
   */
  private static serializeJweGeneralJson (_token: JweToken): string {
    return '';
  }


  /**
   * Serialize a Jwe token object from a token in Flat Json format
   * @param token Jwe base object
   */
  private static serializeJweFlatJson (_token: JweToken): string {
    return '';
  }

  /**
   * Serialize a Jwe token object from a token in Compact format
   * @param token Jwe base object
   */
  private static serializeJweCompact (_token: JweToken): string {
    return '';
  }
  //#endregion
  //#region create

  /**
   * Set the protected header
   * @param protectedHeader to set on the JweToken object
   */
  /*
  private setProtected(protectedHeader: string | JweHeader ) {
    if (typeof protectedHeader === 'string' ) {
      const json = base64url.decode(protectedHeader);
      return <JweHeader>JSON.parse(json);
    }

    return protectedHeader;
  }
*/

//#endregion
  //#region options
  /**
   * Get the keyStore to be used
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  //private getKeyStore(newOptions?: IEncryptionOptions, manadatory: boolean = true): IKeyStore {
  //  return this.getCryptoFactory(newOptions, manadatory).keyStore;
  //}

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
  private getKeyEncryptionKey(newOptions?: IEncryptionOptions, manadatory: boolean = true): Buffer {
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
   * Get the default protected header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  //private getProtected(newOptions?: IEncryptionOptions, manadatory: boolean = false): JweHeader {
  //  return JoseHelpers.getOptionsProperty<JweHeader>('protected', this.options, newOptions, manadatory);
  //}

  /**
   * Get the default header to be used from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  //private getHeader(newOptions?: IEncryptionOptions, manadatory: boolean = false): JweHeader {
  //  return JoseHelpers.getOptionsProperty<JweHeader>('header', this.options, newOptions,manadatory);
  //}

  /**
   * Get the content encryption algorithm from the options
   * @param newOptions Options passed in after the constructure
   * @param manadatory True if property needs to be defined
   */
  private getContentEncryptionAlgorithm(newOptions?: IEncryptionOptions, manadatory: boolean = true): string {
    return JoseHelpers.getOptionsProperty<string>('contentEncryptionAlgorithm', this.options, newOptions, manadatory);
  }
  //#endregion

  /**
   * Encrypt content using the given public keys in JWK format.
   * The key type enforces the key encryption algorithm.
   * The options can override certain algorithm choices.
   * 
   * @param recipients List of recipients' public keys.
   * @param payload to sign.
   * @param format of the final signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in compact Jwe format.
   */
  public async encrypt (recipients: PublicKey[], payload: string, format: ProtectionFormat, options?: IEncryptionOptions): Promise<JweToken> {
    const cryptoFactory: CryptoFactory = this.getCryptoFactory(options);
    const contentEncryptionAlgorithm = this.getContentEncryptionAlgorithm(options);
    this.format = format;

    // encoded protected header
    let encodedProtected: string = '';

    // Set the resulting token
    const jweToken: JweToken = new JweToken();

    // Set the content encryption key
    const contentEncryptionKey: Buffer = this.getKeyEncryptionKey(options);

    // Get the encryptor extensions
    const encryptor = new SubtleCryptoExtension(cryptoFactory);
    

    for (let inx = 0 ; inx < recipients.length; inx ++) {
      // Set the recipients structure
      const jweRecipient = new JweRecipient();
      jweToken.recipients.push(jweRecipient);

      // Decide key encryption algorithm based on given JWK.
      const publicKey: PublicKey = recipients[inx];
      let keyEncryptionAlgorithm: string  | undefined = recipients[inx].alg;
      if (!keyEncryptionAlgorithm) {
        if (publicKey.kty == KeyType.EC) {
          throw new Error('EC encryption not implemented');
        } else {
          // Default RSA algorithm
          keyEncryptionAlgorithm = JoseConstants.RsaOaep;
        }
      }

      if (!JoseHelpers.headerHasElements(jweToken.protected) ) {
        // tslint:disable: no-backbone-get-set-outside-model
        jweToken.protected.set('alg', keyEncryptionAlgorithm);
        jweToken.protected.set('enc', contentEncryptionAlgorithm);
    
        if (publicKey.kid) {
          jweToken.protected.set('kid', publicKey.kid);
        }
        
        encodedProtected = JoseHelpers.headerHasElements(jweToken.protected) ?  
          JoseHelpers.encodeHeader(jweToken.protected) :
          '';
      }


      // Get key encrypter and encrypt the content key
      jweRecipient.encrypted_key = Buffer.from(
        await encryptor.encryptByJwk(
          CryptoHelpers.jwaTow3c(keyEncryptionAlgorithm),
          publicKey,
          contentEncryptionKey));
    }

    // Set the initial vector
    jweToken.iv = this.getInitialVector(options);

    // Set aad as the protected header
    jweToken.aad = base64url.toBuffer(encodedProtected);

    // encrypt content
    const contentEncryptorKey: JsonWebKey = {
      k: base64url.encode(contentEncryptionKey),
      kty: 'oct'
    };

    jweToken.ciphertext = Buffer.from(
      await encryptor.encryptByJwk(
        CryptoHelpers.jwaTow3c(contentEncryptionAlgorithm),
        contentEncryptorKey,
        Buffer.from(payload)));
    return jweToken;
  }
}
