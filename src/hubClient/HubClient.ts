/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import Commit from '../hubSession/Commit';
import UserAgentError from '../UserAgentError';
import CommitSigner from '../hubSession/crypto/CommitSigner';
import HubCommitWriteRequest from '../hubSession/requests/HubCommitWriteRequest';
import HubObjectQueryRequest from '../hubSession/requests/HubObjectQueryRequest';
import HubSession, { HubSessionOptions } from '../hubSession/HubSession';
import IHubClient from './IHubClient';
import HubCommitQueryRequest from '../hubSession/requests/HubCommitQueryRequest';
import HubObject from './HubObject';

/**
 * Class defining options for the
 * HubClient, such as hub Identifier and client Identifier.
 */
export class HubClientOptions {
  /**
   * The Identifier of the owner of the hub.
   */
  hubOwner: Identifier | undefined;

  /**
   * The Identifier of the Client that wants to start hub session.
   */
  clientIdentifier: Identifier | undefined;

  /**
   * Key reference to private key to be used to sign commits and create HubSession
   */
  keyReference: string | undefined;
}

/**
 * Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
 * In a Hub.
 */
export default class HubClient implements IHubClient {

  public hubOwner: Identifier;

  public clientIdentifier: Identifier;

  private readonly keyReference: string;

  /**
   * Constructs an instance of the Hub Client Class for hub operations
   * @param hubClientOptions hub client options used to create instance.
   */
  constructor (hubClientOptions: HubClientOptions) {

    if (!hubClientOptions.hubOwner || !hubClientOptions.clientIdentifier || !hubClientOptions.keyReference) {
      throw new UserAgentError(`HubClientOptions does not contain all properties`);
    }
    this.hubOwner = hubClientOptions.hubOwner;
    this.clientIdentifier = hubClientOptions.clientIdentifier;
    this.keyReference = hubClientOptions.keyReference;
  }

  /**
   * 
   * @param commit Signs and sends a commit to the hub owner's hub.
   */
  public async commit (commit: Commit) {

    if (commit.getProtectedHeaders().iss !== this.clientIdentifier.id) {
      throw new UserAgentError(`Issuer, '${commit.getProtectedHeaders().iss},' is not valid for this HubClient Instance.`);
    }

    if (commit.getProtectedHeaders().sub !== this.hubOwner.id) {
      throw new UserAgentError(`Subject, '${commit.getProtectedHeaders().sub},' is not valid for this HubClient Instance.`);
    }

    if (!this.clientIdentifier.options || !this.clientIdentifier.options.keyStore) {
      throw new UserAgentError(`No KeyStore defined for '${this.clientIdentifier}`);
    }

    const session = await this.createHubSession();

    const commitSignerOptions = {
      did: this.clientIdentifier.id, 
      keyReference: this.keyReference,
      keyStore: this.clientIdentifier.options.keyStore
    };

    const commitSigner = new CommitSigner(commitSignerOptions);

    const signedCommit = await commitSigner.sign(commit);

    const commitRequest = new HubCommitWriteRequest(signedCommit);
    session.send(commitRequest);
  }

  /**
   * Query Objects of certain type in Hub.
   * @param queryRequest object that tells the hub what objec to get.
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
   */
  public async queryObject (commitQueryRequest: HubCommitQueryRequest, hubObject: HubObject): Promise<HubObject> {
    const session = await this.createHubSession();
    await hubObject.setPayload(session, commitQueryRequest);
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
      clientPrivateKeyReference: this.keyReference,
      hubId: 'did:test:hub.id',
      hubEndpoint: 'https://beta.discover.did.microsoft.com'
    };
    return new HubSession(options);
  }
}
