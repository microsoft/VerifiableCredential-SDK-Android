/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import Commit from '../hubSession/Commit';
import HubClient from '../hubClient/HubClient';
import HubObjectQueryRequest from '../hubSession/requests/HubObjectQueryRequest';
import HubObject from '../hubClient/HubObject';
import HubCommitQueryRequest from '../hubSession/requests/HubCommitQueryRequest';

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
  Action = 'Actions',
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
 * Interface for defining options for HubMethods such as hubSession, commitSigner, and hubInterface.
 */
export class HubInterfaceOptions {

  /**
   * Hub Client that will be used to commit and query a hub.
   */
  hubClient: HubClient | undefined;

  /**
   * the schema for the object that will be committed.
   */
  context: string | undefined;

  /**
   * the type of the object that will be committed.
   */
  type: string | undefined;

  /**
   * Optional Commit Strategy to define what strategy to use when compiling commits.
   */
  commitStrategy: CommitStrategyType = CommitStrategyType.Basic;

  /**
   * Optional Hub Interface to define the type of interface the hub request payload will be.
   */
  hubInterface?: HubInterfaceType;
}

/**
 * An Abstract Class for Hub Interfaces.
 * 
 */
export default abstract class HubInterface {

  private hubInterface: HubInterfaceType;
  private commitStrategy: CommitStrategyType;
  private type: string;
  private context: string;
  private hubClient: HubClient;

  /**
   * Creates an instance of HubMethods that will be used to send hub requests and responses.
   * @param [hubInterfaceOptions] for configuring how to form hub requests and responses.
   */
  constructor (hubInterfaceOptions: HubInterfaceOptions) {

    if (!hubInterfaceOptions.context || !hubInterfaceOptions.type || !hubInterfaceOptions.hubClient) {
      throw new UserAgentError(`Hub Interface Options missing parameters`);
    }
    
    this.context = hubInterfaceOptions.context;
    this.type = hubInterfaceOptions.type;
    this.hubClient = hubInterfaceOptions.hubClient;

    if (!hubInterfaceOptions.hubInterface) {
      throw new UserAgentError('Hub Interface is not defined in the Hub Method Options');
    }
    this.hubInterface = hubInterfaceOptions.hubInterface;
    this.commitStrategy = hubInterfaceOptions.commitStrategy;
  }

  public async addItem(payload: any): Promise<void> {

    const commit = new Commit({
      protected: {
        committed_at: (new Date()).toISOString(),
        iss: this.hubClient.clientIdentifier.id,
        sub: this.hubClient.hubOwner.id,
        interface: this.hubInterface,
        context: this.context,
        type: this.type,
        operation: Operation.Create,
        commit_strategy: this.commitStrategy,
      },
      payload
    });
    this.hubClient.commit(commit);
  }
  
  /**
   * Get items with just metadata.
   */
  public async getPartialItems() {
  
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
   * create and return fully-formed hubObject.
   * @param hubObject partial hubObject with metadata
   */
  public async getItem(hubObject: HubObject): Promise<HubObject> {

    const metadata = hubObject.getMetadata();

    const commitQueryRequest = new HubCommitQueryRequest({
      object_id: [metadata.id],
    });
    return this.hubClient.queryObject(commitQueryRequest, hubObject);
  }

  /**
   * Get a list of fully-formed HubObjects with metadata and payload.
   */
  public async getFullItems(): Promise<HubObject[]> {
    const items = await this.getPartialItems();

    items.forEach(async item => {
      item = await this.getItem(item); 
    });

    return items;
  }

  public async updateItem() {
    throw new UserAgentError('Not Implemented');
  }

  public async deleteItem() {
    throw new UserAgentError('Not Implemented');
  }
}
