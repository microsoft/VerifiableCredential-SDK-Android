/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import Commit, { ICommitFields } from '../hubSession/Commit';
import HubClient from '../hubClient/HubClient';
import HubObject from '../hubClient/HubObject';
import HubObjectQueryRequest from '../hubSession/requests/HubObjectQueryRequest';
import HubCommitQueryRequest from '../hubSession/requests/HubCommitQueryRequest';
import { HubClientOptions } from '../hubClient/IHubClient';

/**
 * Constants that represent what type of commit strategy to be used.
 */
export enum CommitStrategyType {
  Basic = 'basic',
  LastWriterWins = 'lastWriterWins'
}

/**
 * Constants that represent what interface type the hub request payload will be.
 */
export enum HubInterfaceType {
  Actions = 'Actions',
  Collections = 'Collections',
  Permissions = 'Permissions',
  Profile = 'Profile'
}

/**
 * Hub Operations
 */
export enum Operation {
  Create = 'create',
  Read = 'read',
  Update = 'update',
  Delete = 'delete'
}

/**
 * Interface for defining options for HubInterface.
 */
export class HubInterfaceOptions extends HubClientOptions {

  /**
   * The schema for the object that will be committed.
   */
  context: string | undefined;

  /**
   * The type of the object that will be committed.
   */
  type: string | undefined;

  /**
   * Commit Strategy to define what strategy to use when compiling commits.
   */
  commitStrategy: CommitStrategyType = CommitStrategyType.Basic;

  /**
   * Hub Interface to define the type of interface the hub request payload will be.
   */
  hubInterface: HubInterfaceType = HubInterfaceType.Collections;
}

/**
 * An Abstract Class for Hub Interfaces.
 * 
 */
export default abstract class HubInterface {

  public readonly hubInterface: HubInterfaceType;
  public readonly commitStrategy: CommitStrategyType;
  public readonly type: string;
  public readonly context: string;
  public hubClient: HubClient;

  /**
   * Creates an instance of HubMethods that will be used to send hub requests and responses.
   * @param [hubInterfaceOptions] for configuring how to form hub requests and responses.
   */
  constructor (hubInterfaceOptions: HubInterfaceOptions) {

    if (!hubInterfaceOptions.context) {
      throw new UserAgentError(`Hub Interface Options missing context parameter`);
    }

    if (!hubInterfaceOptions.type) {
      throw new UserAgentError(`Hub Interface Options missing type parameter`);
    }
    
    this.context = hubInterfaceOptions.context;
    this.type = hubInterfaceOptions.type;
    this.hubInterface = hubInterfaceOptions.hubInterface;
    this.commitStrategy = hubInterfaceOptions.commitStrategy;

    // set up hub client
    const hubClientOptions: HubClientOptions = {
      hubOwner: hubInterfaceOptions.hubOwner,
      clientIdentifier: hubInterfaceOptions.clientIdentifier,
      recipientsPublicKeys: hubInterfaceOptions.recipientsPublicKeys,
      hubProtectionStrategy: hubInterfaceOptions.hubProtectionStrategy,
      cryptoOptions: hubInterfaceOptions.cryptoOptions
    };
    this.hubClient = new HubClient(hubClientOptions);
  }

  /**
   * Add object to Hub Owner's hub.
   * @param object object to be added to hub owned by hub owner.
   */
  public async addObject(object: any): Promise<any> {

    const commit = new Commit(<ICommitFields>{
      committed_at: (new Date()).toISOString(),
      iss: this.hubClient.clientIdentifier.id,
      sub: this.hubClient.hubOwner.id,
      interface: this.hubInterface,
      context: this.context,
      type: this.type,
      operation: Operation.Create,
      commit_strategy: this.commitStrategy,
      payload: object
    });
    return this.hubClient.commit(commit);
  }
  
  /**
   * Get all unhydrated hubObjects of specific type.
   */
  public async getUnHydratedObjects(): Promise<HubObject[]> {
  
    const queryRequest = new HubObjectQueryRequest({
      interface: this.hubInterface,
      context: this.context,
      type: this.type,
    });

    const objects = await this.hubClient.queryObjects(queryRequest);
    console.log(objects);
    return objects;
  }

  /**
   * create and return hydrated hubObject.
   * @param hubObject unhydrated hubObject containing on object metadata.
   */
  public async getObject(hubObject: HubObject): Promise<HubObject> {

    const metadata = hubObject.getMetadata();

    const commitQueryRequest = new HubCommitQueryRequest({
      object_id: [metadata.id],
    });
    return this.hubClient.queryObject(commitQueryRequest, hubObject);
  }

  /**
   * Get a list of all hydrated HubObjects containing both metadata and payload.
   */
  public async getObjects(): Promise<HubObject[]> {
    const objects = await this.getUnHydratedObjects();

    objects.forEach(async obj => {
      obj = await this.getObject(obj); 
    });

    return objects;
  }

  /**
   * Update Hub Object in hub owner's hub.
   */
  public async updateObject(hubObject: HubObject) {
    throw new UserAgentError(`Not Implemented for ${hubObject}`);
  }

  /**
   * Update Hub Object in hub owner's hub.
   */
  public async deleteObject(hubObject: HubObject) {
    throw new UserAgentError(`Not Implemented for ${hubObject}`);
  }
}
