/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { DidKey, KeyExport, KeyUseFactory } from '@decentralized-identity/did-common-typescript';
import IKeyStore from './keystores/IKeyStore';
import KeyStoreConstants from './keystores/KeyStoreConstants';

import { PublicKey } from './types';
import IdentifierDocument from './IdentifierDocument';
import UserAgentOptions from './UserAgentOptions';
import UserAgentError from './UserAgentError';

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
   * Constructs an instance of the Identifier
   * class using the provided identifier or identifier document.
   * @param identifier either the string representation of an identifier or a identifier document.
   * @param [options] for configuring how to register and resolve identifiers.
   */
  constructor (public identifier: IdentifierDocument | string, private options?: UserAgentOptions) {
    // Check whether passed an identifier document
    // or an identifier string
    if (typeof identifier === 'object') {
      this.document = identifier;
      this.id = identifier.id;
    } else {
      this.id = identifier;
    }
  }

  /**
   * Creates a new decentralized identifier.
   * @param [options] for configuring how to register and resolve identifiers.
   */
  public static async create (options: UserAgentOptions): Promise<Identifier> {
    const id = 'did:ion';
    return new Identifier(id, options).createLinkedIdentifier(id);
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
      const keyStore: IKeyStore = this.options.keyStore;
      const seed: Buffer = await keyStore.get(KeyStoreConstants.masterSeed) as Buffer;

      // Create DID key
      const didKey = new DidKey(this.options.cryptoOptions!.cryptoApi, this.options.cryptoOptions!.algorithm);
      const pairwiseKey: DidKey = await didKey.generatePairwise(seed, this.id, target);
      const jwk: any = await pairwiseKey.getJwkKey(KeyExport.Private);

      const pairwiseKeyStorageId = Identifier.keyStorageIdentifier(this.id, target, KeyUseFactory.create(pairwiseKey.algorithm), jwk.kty);
      await keyStore.save(pairwiseKeyStorageId, jwk);

      // Set key format
      const publicKey: PublicKey = {
        id: jwk.kid,
        type: this.getDidDocumentKeyType(),  // TODO switch by leveraging pairwiseKey
        publicKeyJwk: jwk
      };

      const document = await this.createIdentifierDocument(this.id, publicKey);
      if (register) {
        if (this.options && this.options.registrar) {
            // register did document
          const identfier = await this.options.registrar.register(document, pairwiseKeyStorageId);
          document.id = identfier.id;
        } else {
          throw new UserAgentError(`No registrar in options to register DID document`);
        }
      }

      return new Identifier(document);

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
      this.document = await this.options.resolver.resolve(this);
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
    if (this.document && this.document.publicKey && keyIdentifier) {
      const index = this.document.publicKey.findIndex(key => key.id === keyIdentifier);
      if (index === -1) {
        throw new UserAgentError(`Document does not contain a key with id '${keyIdentifier}'`);
      }

      return this.document.publicKey[index];
    } else if (this.document && this.document.publicKey && this.document.publicKey.length > 0) {
      // If only one key has been specified in the document
      // return that
      return this.document.publicKey[0];
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
    return IdentifierDocument.createAndGenerateId(id, [ publicKey ], this.options as UserAgentOptions);
  }

  // Get the did document public key type
  private getDidDocumentKeyType () {
    // Support other key types
    return 'Secp256k1VerificationKey2018';
  }
}
