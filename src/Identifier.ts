/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { DidKey, KeyExport } from '@decentralized-identity/did-common-typescript';
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
      const didKey: any = new DidKey(this.options.cryptoOptions!.cryptoApi, this.options.cryptoOptions!.algorithm);
      const pairwiseKey: Buffer | DidKey = await didKey.generatePairwise(seed, this.id, target);

      // TODO add key type in the storage identfier
      const pairwiseKeyStorageId = this.pairwiseKeyStorageIdentifier(this.id, target);
      await keyStore.save(pairwiseKeyStorageId, pairwiseKey);
      const document = await this.createIdentifierDocument(this.id, pairwiseKey as DidKey);
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
    if (this.document && this.document.publicKeys && keyIdentifier) {
      const index = this.document.publicKeys.findIndex(key => key.id === keyIdentifier);
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

  // Create an identifier document. Included the public key.
  private async createIdentifierDocument (id: string, key: DidKey): Promise <IdentifierDocument> {
    const publicKeyJwk = await this.getDidPublicKey(key);
    return IdentifierDocument.createAndGenerateId(id, [ publicKeyJwk ], this.options as UserAgentOptions);
  }

  // Retrieve the public key from a DidKey
  private async getDidPublicKey (key: DidKey): Promise <any> {
    const jwk = await key.getJwkKey(KeyExport.Public);
    return jwk;
  }

  // Generate a storage identifier to store a pairwise key
  private pairwiseKeyStorageIdentifier (personaId: string, target: string): string {
    return `${personaId}-${target}`;
  }
}
