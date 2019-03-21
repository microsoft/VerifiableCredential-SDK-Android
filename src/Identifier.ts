/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { DidKey, KeyExport } from '@decentralized-identity/did-common-typescript';
import { PublicKey } from './types';
import IdentifierDocument from './IdentifierDocument';
import UserAgentOptions from './UserAgentOptions';
import UserAgentError from './UserAgentError';
import KeyStore from './keystores/KeyStore';
import KeyStoreConstants from './keystores/KeyStoreConstants';

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
  constructor (identifier: IdentifierDocument | string, private options?: UserAgentOptions) {
    // Check whether passed an identifier document
    // or an identifier string
    this.id = ''; // Avoid tslint error
    this.identifier = identifier;
  }

  /**
   * Gets the identifier
   */
  public get identifier (): IdentifierDocument | string {
    if (this.document) {
      return this.document;
    }

    return this.id;
  }

  /**
   * Sets the identifier
   */
  public set identifier (identifierOrDocument: IdentifierDocument | string) {
    if (typeof identifierOrDocument === 'object') {
      this.document = identifierOrDocument;
      this.id = identifierOrDocument.id;
    } else {
      this.id = identifierOrDocument;
    }
  }

  /**
   * Creates a new decentralized identifier.
   * If the registar flag is true, the newly created
   * identifier will be registered using the options
   * @param register flag indicating whether the new identifier should be registered
   * with a ledger.
   */
  public async create (register: boolean = false): Promise<IdentifierDocument> {
    return this.createLinkedIdentifier(this.id, register);
  }

  /**
   * Creates a new decentralized identifier, using the current identifier
   * and the specified target. If the registar flag is true, the newly created
   * identifier will be registered using the
   * @param target entity for which to create the linked identifier
   * @param register flag indicating whether the new identifier should be registered
   * with a ledger.
   */
  public async createLinkedIdentifier (target: string, register: boolean = false): Promise<IdentifierDocument> {
    if (this.options && this.options.keyStore) {
      let keyStore: KeyStore = this.options.keyStore;
      let seed: Buffer = await keyStore.get(KeyStoreConstants.masterSeed);

      // Create DID key
      let didKey: any = new DidKey(this.options.cryptoOptions!.cryptoApi, this.options.cryptoOptions!.algorithm);
      let pairwiseKey: Buffer | DidKey = await didKey.generatePairwise(seed, this.id, target);

      // TODO add key type in the storage identfier
      let success: boolean = await keyStore.save(this.pairwiseKeyStorageIdentifier(this.id, target), pairwiseKey);
      if (success) {
        let document = await this.createIdentifierDocument(this.id, pairwiseKey as DidKey, this.options);
        if (register) {
          if (this.options && this.options.registrar) {
            // register did document
            const identfier = await this.options.registrar.register(document);
            document.id = identfier.id;
          } else {
            throw new UserAgentError(`No registrar in options to register DID document`);
          }
        }

        return document;
      } else {
        let message = `Error while saving pairwise key for DID '${this.id}' to key store.`;
        throw new UserAgentError(message);
      }
    }
    throw new UserAgentError('No keyStore in options');
  }

  /**
   * Gets the IdentifierDocument for the identifier
   * instance, throwing if no identifier has been
   * created.
   */
  public async getDocument (): Promise <IdentifierDocument> {
    // If we already have not already
    // retrieved the document use the
    // resolver to get the document
    if (!this .document) {
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
  public async getPublicKey (keyIdentifier ?: string): Promise <PublicKey> {
    if (!this .document) {
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
  private async createIdentifierDocument (id: string, key: DidKey, options: UserAgentOptions): Promise <IdentifierDocument> {
    let publicKeyJwk = await this.getDidPublicKey(key);
    return IdentifierDocument.createAndGenerateId(id, [ publicKeyJwk ], options);
  }

  // Retrieve the public key from a DidKey
  private async getDidPublicKey (key: DidKey): Promise <any> {
    let jwk = await key.getJwkKey(KeyExport.Public);
    return jwk;
  }

  // Generate a storage identifier to store a pairwise key
  private pairwiseKeyStorageIdentifier (personaId: string, target: string): string {
    return `${personaId}-${target}`;
  }
}
