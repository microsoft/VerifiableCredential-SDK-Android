/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import Commit from '../hubSession/Commit';
import HubClient from '../hubClient/HubClient';

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
  
  public async getItems() {
    throw new UserAgentError('Not Implemented');
  }

  public async getItem() {
    throw new UserAgentError('Not Implemented');
  }

  public async updateItem() {
    throw new UserAgentError('Not Implemented');
  }

  public async deleteItem() {
    throw new UserAgentError('Not Implemented');
  }
}
