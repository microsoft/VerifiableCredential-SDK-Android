/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import KeyStoreConstants from './keystores/KeyStoreConstants';
import { PublicKey } from './types';
import IdentifierDocument from './IdentifierDocument';
import UserAgentOptions from './UserAgentOptions';
import UserAgentError from './UserAgentError';
import IKeyStore from './crypto/keyStore/IKeyStore';
import { ProtectionFormat } from './crypto/keyStore/ProtectionFormat';
import PairwiseKey from './crypto/keys/PairwiseKey';
import SubtleCryptoExtension from './crypto/plugin/SubtleCryptoExtension';
import KeyUseFactory from './crypto/keys/KeyUseFactory';

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
      const algorithm = this.options.cryptoOptions!.algorithm;
      const generator = new SubtleCryptoExtension(cryptoFactory);
      const jwk = await generator.generatePairwiseKey(algorithm, KeyStoreConstants.masterSeed, this.id, target, true, ['sign']);
      pubJwk.kid = jwk.kid;

      const pairwiseKeyStorageId = Identifier.keyStorageIdentifier(this.id, target, KeyUseFactory.create(algorithm), <string>jwk.kty);
      await this.options.keyStore.save(pairwiseKeyStorageId, jwk);

      // Set key format
      // todo switch by leveraging pairwiseKey
      const publicKey: PublicKey = {
        id: jwk.kid,
        type: this.getDidDocumentKeyType(),
        publicKeyJwk: pubJwk
      };
      if (this.options.registrar) {
        const document = await this.createIdentifierDocument(this.id, publicKey);
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
  public async getPublicKey (keyIdentifier?: string): Promise<PublicKey> {
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
   * @param keyUse Key usage
   * @param keyType Key type
   */
  public static keyStorageIdentifier (personaId: string, target: string, keyUse: string, keyType: string): string {
    return `${personaId}-${target}-${keyUse}-${keyType}`;
  }

  // Create an identifier document. Included the public key.
  private async createIdentifierDocument (id: string, publicKey: PublicKey): Promise <IdentifierDocument> {
    return IdentifierDocument.createAndGenerateId(id, [ publicKey ], <UserAgentOptions> this.options);
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
  public async sign (payload: any, personaId: string, target: string) {
    let body: string;
    if (this.options && this.options.cryptoOptions) {
      const keyStorageIdentifier = Identifier.keyStorageIdentifier(personaId,
                                                                   target,
                                                                   KeyUse.Signature,
                                                                   KeyTypeFactory.create(this.options.cryptoOptions.algorithm));
      if (this.options.keyStore) {
        if (typeof(payload) !== 'string') {
          body = JSON.stringify(payload);
        } else {
          body = payload;
        }
        return this.options.keyStore.sign(keyStorageIdentifier, body, ProtectionFormat.JwsFlatJson);
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
  public async verify (jws: string) {

    if (!this.document) {
      this.document = await this.getDocument();
    }
    return Protect.verify(jws, this.document.publicKeys);
  }
}
