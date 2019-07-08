/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import KeyStoreConstants from './keystores/KeyStoreConstants';
import IdentifierDocument from './IdentifierDocument';
import UserAgentOptions from './UserAgentOptions';
import UserAgentError from './UserAgentError';
import { ProtectionFormat } from './crypto/keyStore/ProtectionFormat';
import SubtleCryptoExtension from './crypto/plugin/SubtleCryptoExtension';
import { KeyUse } from './crypto/keys/KeyUseFactory';
import JwsToken from './crypto/protocols/jose/jws/JwsToken';
import PrivateKey from './crypto/keys/PrivateKey';
import { IJwsSigningOptions, IJweEncryptionOptions } from "./crypto/protocols/jose/IJoseOptions";
import { IdentifierDocumentPublicKey } from './types';
import CryptoHelpers from './crypto/utilities/CryptoHelpers';
import KeyUseFactory from './crypto/keys/KeyUseFactory';
import JweToken from './crypto/protocols/jose/jwe/JweToken';
import JoseConstants from './crypto/protocols/jose/JoseConstants';
import CryptoFactory from './crypto/plugin/CryptoFactory';

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 */
export default class Identifier {
  /**
   * The string representation of the identier for the persona
   * in the format 'did:{method}:{id}'.
   */
  public id: string;

  /**
   * The identifier document for the identifier
   * if one exists.
   */
  public document: IdentifierDocument | undefined;

  /**
   * User Agent Options
   */
  public options: UserAgentOptions | undefined;

  /**
   * Constructs an instance of the Identifier
   * class using the provided identifier or identifier document.
   * @param identifier either the string representation of an identifier or a identifier document.
   * @param [options] for configuring how to register and resolve identifiers.
   */
  constructor (public identifier: IdentifierDocument | string, options?: UserAgentOptions) {
    // Check whether passed an identifier document
    // or an identifier string
    if (typeof identifier === 'object') {
      this.document = identifier;
      this.id = identifier.id;
    } else {
      this.id = identifier;
    }
    this.options = options;
  }

  /**
   * Creates a new decentralized identifier.
   * @param [options] for configuring how to register and resolve identifiers.
   */
  public static async create (options: UserAgentOptions): Promise<Identifier> {
    const id = <string> options.didPrefix;
    return new Identifier(id, options).createLinkedIdentifier(id, true);
  }

  /**
   * Creates a new decentralized identifier, using the current identifier
   * and the specified target. If the registar flag is true, the newly created
   * identifier will be registered using the
   * @param target entity for which to create the linked identifier
   * @param register flag indicating whether the new identifier should be registered
   * with a ledger.
   */
  public async createLinkedIdentifier (target: string, register: boolean = false): Promise<Identifier> {
    if (this.options && this.options.keyStore) {

      // Create DID key
      const cryptoFactory = this.options.cryptoFactory;
      const signingAlgorithm = CryptoHelpers.jwaToWebCrypto(this.options.cryptoOptions.authenticationSigningJoseAlgorithm)
      const generator = new SubtleCryptoExtension(cryptoFactory);
      const jwk: PrivateKey = await generator.generatePairwiseKey(signingAlgorithm, KeyStoreConstants.masterSeed, this.id, target);
      const pubJwk = jwk.getPublicKey();
      
      pubJwk.kid = jwk.kid;

      const pairwiseKeyStorageId = Identifier.keyStorageIdentifier(
        this.id, 
        target, 
        this.options.cryptoOptions.authenticationSigningJoseAlgorithm, 
        KeyUseFactory.createViaJwa(this.options.cryptoOptions.authenticationSigningJoseAlgorithm));
      await this.options.keyStore.save(pairwiseKeyStorageId, jwk);

      // Set key format
      // todo switch by leveraging pairwiseKey
      const publicKey: IdentifierDocumentPublicKey = {
        id: <string>jwk.kid,
        type: this.getDidDocumentKeyType(),
        publicKeyJwk: pubJwk
      };
      if (this.options.registrar) {
        const document = await this.createIdentifierDocument(this.id, [publicKey]);
        if (register) {
            // register did document
          const identifier = await this.options.registrar.register(document, pairwiseKeyStorageId);
          document.id = identifier.id;
        }
        return new Identifier(document, this.options);
      } else {
        throw new UserAgentError(`No registrar in options to register DID document`);
      }
    }
    throw new UserAgentError('No keyStore in options');
  }

  /**
   * Gets the IdentifierDocument for the identifier
   * instance, throwing if no identifier has been
   * created.
   */
  public async getDocument (): Promise<IdentifierDocument> {
    // If we already have not already
    // retrieved the document use the
    // resolver to get the document
    if (!this.document) {
      if (!this.options || !this.options.resolver) {
        throw new UserAgentError('Resolver not specified in user agent options.');
      }

      // We need to resolve the document
      this.document = <IdentifierDocument> await this.options.resolver.resolve(this);
    }

    return this.document;
  }

  /**
   * Performs a public key lookup using the
   * specified key identifier, returning the
   * key defined in document.
   * @param keyIdentifier the identifier of the public key.
   */
  public async getPublicKey (keyIdentifier?: string): Promise<IdentifierDocumentPublicKey> {
    if (!this.document) {
      await this.getDocument();
    }

    // If we have been provided a key identifier use
    // the identifier to look up a key in the document
    if (this.document && this.document.publicKeys && keyIdentifier) {
      const index = this.document.publicKeys.findIndex((key: any) => key.id === keyIdentifier);
      if (index === -1) {
        throw new UserAgentError(`Document does not contain a key with id '${keyIdentifier}'`);
      }

      return this.document.publicKeys[index];
    } else if (this.document && this.document.publicKeys && this.document.publicKeys.length > 0) {
      // If only one key has been specified in the document
      // return that
      return this.document.publicKeys[0];
    }

    throw new UserAgentError('Document does not contain any public keys');
  }

  /**
   * Generate a storage identifier to store a key
   * @param personaId The identifier for the persona
   * @param target The identifier for the peer. Will be persona for non-pairwise keys
   * @param algorithm Key algorithm
   * @param keyType Key type
   */
  public static keyStorageIdentifier (personaId: string, target: string, algorithm: string, keyType: string): string {
    console.log(`${personaId}-${target}-${algorithm}-${keyType}`);
    return `${personaId}-${target}-${algorithm}-${keyType}`;
  }

  // Create an identifier document. Included the public key.
  private async createIdentifierDocument (id: string, publicKeys: IdentifierDocumentPublicKey[]): Promise <IdentifierDocument> {
    return IdentifierDocument.createAndGenerateId(id, publicKeys, <UserAgentOptions> this.options);
  }

  // Get the did document public key type
  private getDidDocumentKeyType () {
    // Support other key types
    return 'Secp256k1VerificationKey2018';
  }

  /**
   * Sign payload with key specified by keyStorageIdentifier in options.keyStore
   * @param payload object to be signed
   * @param keyStorageIdentifier the identifier for the key used to sign payload.
   */
  public async sign (payload: any, keyReference: string): Promise<string> {
    let body: string;
    if (this.options && this.options.cryptoOptions) {
      if (this.options.keyStore) {
        if (typeof(payload) !== 'string') {
          body = JSON.stringify(payload);
        } else {
          body = payload;
        }
        const signingOptions: IJwsSigningOptions = {
          cryptoFactory: this.options.cryptoFactory
        };
        const jws = new JwsToken(signingOptions);
        const signature = await jws.sign(keyReference, Buffer.from(body), ProtectionFormat.JwsFlatJson);
        return signature.serialize();;
      } else {
        throw new UserAgentError('No KeyStore in Options');
      }
    } else {
      throw new UserAgentError('No Crypto Options in User Agent Options');
    }
  }

  /**
   * Verify the payload with public key from the Identifier Document.
   * @param jws the signed token to be verified.
   */
  public async verify (jws: string): Promise<string> {

    if (!this.document) {
      this.document = await this.getDocument();
    }
    const signingOptions: IJwsSigningOptions = {
      cryptoFactory: (<UserAgentOptions>this.options).cryptoFactory
    };
    const token = JwsToken.deserialize(jws, signingOptions);
    if (token.verify(this.document.getPublicKeysFromDocument(), signingOptions)) {
      return token.getPayload();
    }

    throw new UserAgentError(`The signature validation for '${this.id}' failed.`);
  }

  /**
   * Encrypt payload using Public Key registered on Identifier Document.
   * @param payload object that will be encrypted.
   */
  public async encrypt (payload: any): Promise<string> {
    if (!this.options) {
      throw new UserAgentError('Options Undefined');
    }
    // get document if undefined
    if (!this.document) {
      this.document = await this.getDocument();
    }

    const keyStore = this.options.keyStore;
    const cryptoFactory = this.options.cryptoFactory;

    const options: IJweEncryptionOptions = {
      cryptoFactory: cryptoFactory,
      contentEncryptionAlgorithm: JoseConstants.AesGcm256
    };

    // create a jweToken with temp cryptoFactory and algorithm.
    const jweToken = new JweToken(options);

    // encrypt payload using public keys.
    const encryptedToken = await jweToken.encrypt(this.document.getPublicKeysFromDocument(), payload, ProtectionFormat.JweCompactJson);

    // return serialized token.
    return encryptedToken.serialize();
  }

  /**
   * Decrypt cipher using key referenced in keystore.
   * @param cipher cipher to be decrypted.
   * @param keyReference string that references what key to use from keystore.
   */
  public async decrypt (cipher: Buffer, keyReference: string): Promise<string> {
    
    if (!this.options) {
      throw new UserAgentError('Options Undefined');
    }

    // get key to get key alg
    const key = <PrivateKey> await this.options.keyStore.get(keyReference);

    // create jweToken, feed in ciphertext, and decrypt.
    const jweToken = new JweToken({cryptoFactory: this.options.cryptoFactory, contentEncryptionAlgorithm: key.alg});
    jweToken.ciphertext = cipher;
    const payload = await jweToken.decrypt(keyReference);
    return payload.toString();
  }
}
