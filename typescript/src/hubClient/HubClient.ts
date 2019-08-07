/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import Commit from '../hubSession/Commit';
import UserAgentError from '../UserAgentError';
import CommitProtector, { CommitProtectorOptions } from '../hubSession/crypto/CommitProtector';
import HubCommitWriteRequest from '../hubSession/requests/HubCommitWriteRequest';
import HubObjectQueryRequest from '../hubSession/requests/HubObjectQueryRequest';
import HubSession, { HubSessionOptions } from '../hubSession/HubSession';
import IHubClient, {HubClientOptions } from './IHubClient';
import HubObject from './HubObject';
import HubCommitQueryRequest from '../hubSession/requests/HubCommitQueryRequest';
import CryptoFactory from '../crypto/plugin/CryptoFactory';
import { IPayloadProtection } from '../crypto/protocols/IPayloadProtection';
import ProtectionStrategy from '../crypto/strategies/ProtectionStrategy';
import { PublicKey, UserAgentOptions } from '..';
import JoseConstants from '../crypto/protocols/jose/JoseConstants';
import IPayloadProtectionOptions from '../crypto/protocols/IPayloadProtectionOptions';
import JoseProtocol from '../crypto/protocols/jose/JoseProtocol';
import { TSMap } from 'typescript-map';

/**
 * Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
 * In a Hub.
 */
export default class HubClient implements IHubClient {

  public hubOwner: Identifier;

  public clientIdentifier: Identifier;

  private readonly signingKeyReference: string | undefined;

  private readonly encryptionKeyReference: string | undefined;

  private readonly recipientsPublicKeys: PublicKey[] | undefined;

  private readonly cryptoFactory: CryptoFactory;

  private readonly payloadProtection: IPayloadProtection;

  private readonly hubProtectionStrategy: ProtectionStrategy | undefined;

  /**
   * Constructs an instance of the Hub Client Class for hub operations
   * @param hubClientOptions hub client options used to create instance.
   */
  constructor (hubClientOptions: HubClientOptions) {

    if (!hubClientOptions.hubOwner || !hubClientOptions.clientIdentifier) {
      throw new UserAgentError(`HubClientOptions does not contain all properties`);
    }
    this.hubOwner = hubClientOptions.hubOwner;
    this.clientIdentifier = hubClientOptions.clientIdentifier;
    this.signingKeyReference = (<UserAgentOptions>hubClientOptions.clientIdentifier.options).cryptoOptions.signingKeyReference;
    this.encryptionKeyReference = (<UserAgentOptions>hubClientOptions.clientIdentifier.options).cryptoOptions.encryptionKeyReference;
    this.recipientsPublicKeys = hubClientOptions.recipientsPublicKeys;
    this.cryptoFactory = hubClientOptions.cryptoOptions.cryptoFactory;
    this.payloadProtection = hubClientOptions.cryptoOptions.payloadProtection;
    this.hubProtectionStrategy = hubClientOptions.hubProtectionStrategy;
  }

  /**
   * Protect and sends a commit to the hub owner's hub.
   * @param commit commit to be sent to hub owner's hub.
   */
  public async commit (commit: Commit) {

    if (commit.getCommitFields().iss !== this.clientIdentifier.id) {
      throw new UserAgentError(`Issuer, '${commit.getCommitFields().iss},' is not valid for this HubClient Instance.`);
    }

    if (commit.getCommitFields().sub !== this.hubOwner.id) {
      throw new UserAgentError(`Subject, '${commit.getCommitFields().sub},' is not valid for this HubClient Instance.`);
    }

    if (!this.clientIdentifier.options || !this.clientIdentifier.options.keyStore) {
      throw new UserAgentError(`No KeyStore defined for '${this.clientIdentifier}`);
    }

    const fields = commit.getCommitFields();
    fields.kid = '';
    
    const map = Object.keys(fields).map((field) => {
      return [field, fields[field]];
    });
    
    const options: IPayloadProtectionOptions = {
      cryptoFactory: this.cryptoFactory,
      options: new TSMap<string, any>([
        [JoseConstants.optionProtectedHeader, new TSMap(map) ]
    ]),
      payloadProtection: new JoseProtocol()
  };

    const commitProtectOptions: CommitProtectorOptions = {
      did: this.clientIdentifier.id, 
      signingKeyReference: this.signingKeyReference,
      recipientsPublicKeys: this.recipientsPublicKeys,
      payloadProtection: this.payloadProtection,
      payloadProtectionOptions: options,
      hubProtectionStrategy: this.hubProtectionStrategy
    };

    // protect the commit
    const commitProtector = new CommitProtector(commitProtectOptions);
    const protectedCommit = await commitProtector.protect(commit);
    const commitRequest = new HubCommitWriteRequest(protectedCommit);

    const session = await this.createHubSession();
    return session.send(commitRequest);
  }

  /**
   * Query Objects of certain type in Hub.
   * @param queryRequest object that tells the hub what object to get.
   */
  public async queryObjects (queryRequest: HubObjectQueryRequest): Promise<HubObject[]> {
    const session = await this.createHubSession();
    const queryResponse = await session.send(queryRequest);

    const objects = queryResponse.getObjects();

    let hubObjects: HubObject[] = [];

    objects.forEach(object => {
      hubObjects.push(new HubObject(object));
    });
    return hubObjects;
  }

  /**
   * Query Object specified by certain id 
   * @param commitQueryRequest HubCommitQueryRequest object to request object of specific id.
   * @param hubObject a HubObject containing metadata such as object id.
   */
  public async queryObject (commitQueryRequest: HubCommitQueryRequest, hubObject: HubObject): Promise<HubObject> {
    const session = await this.createHubSession();
    await hubObject.hydrate(session, commitQueryRequest);
    return hubObject;

  }

  /**
   * Get all Hub Instances from hub owner's identifier document.
   */
  public async getHubInstances () {
    const identifierDocument = await this.hubOwner.getDocument();
    return identifierDocument.getHubInstances();
  }

  /**
   * Implement createHubSession method once HubSession is refactored.
   * creates a hubSession for hub instance that is available/online.
   */
  private async createHubSession () {
    const options: HubSessionOptions = {
      client: this.clientIdentifier,
      hubOwner: this.hubOwner,
      hubId: 'did:ion:test:EiANVcAJKiAdVQRId8IYrQHxJE4nU4-_pW041qh-z5tVZQ',
      hubEndpoint: 'http://localhost:8080/',
      signingKeyReference: this.signingKeyReference,
      encryptionKeyReference: this.encryptionKeyReference
    };
    return new HubSession(options);
  }
}
