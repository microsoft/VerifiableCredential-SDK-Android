/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import Commit from '../hubSession/Commit';
import HubClient from '../HubClient';

/**
 * Constants that represent what type of commit strategy to be used.
 */
export enum CommitStrategyReference {
  Basic = 'basic',
  LastWriterWins = 'lastWriterWins'
}

/**
 * Constants that represent what interface type the hub request payload will be.
 */
export enum HubInterface {
  Action = 'Actions',
  Collections = 'Collections',
  Permissions = 'Permissions',
  Profile = 'Profile'
}

/**
 * Interface for defining options for HubMethods such as hubSession, commitSigner, and hubInterface.
 */
export interface HubMethodsOptions {

  /**
   * Hub Client that will be used to commit and query a hub.
   */
  hubClient: HubClient;

  /**
   * the schema for the object that will be committed.
   */
  context: string;

  /**
   * the type of the object that will be committed.
   */
  type: string;

  /**
   * Optional Commit Strategy to define what strategy to use when compiling commits.
   */
  commitStrategy?: CommitStrategyReference;

  /**
   * Optional Hub Interface to define the type of interface the hub request payload will be.
   */
  hubInterface?: HubInterface;
}

/**
 * An Abstract Class for HubMethods.
 * 
 */
export default abstract class HubMethods {

  private hubInterface: HubInterface;
  private commitStrategy: CommitStrategyReference;
  private type: string;
  private context: string;
  private hubClient: HubClient;

  /**
   * Creates an instance of HubMethods that will be used to send hub requests and responses.
   * @param [hubMethodOptions] for configuring how to form hub requests and responses.
   */
  constructor (hubMethodOptions: HubMethodsOptions) {
    
    this.context = hubMethodOptions.context;
    this.type = hubMethodOptions.type;
    this.hubClient = hubMethodOptions.hubClient;

    if (!hubMethodOptions.hubInterface) {
      throw new UserAgentError('Hub Interface is not defined in the Hub Method Options');
    }
    this.hubInterface = hubMethodOptions.hubInterface;

    if (!hubMethodOptions.commitStrategy) {
      this.commitStrategy = CommitStrategyReference.Basic;
    } else {
      this.commitStrategy = hubMethodOptions.commitStrategy;
    }
  }

  public async addItem(payload: any): Promise<void> {
    
    const create = 'create';

    const commit = new Commit({
      protected: {
        committed_at: (new Date()).toISOString(),
        iss: this.hubClient.clientIdentifier.id,
        sub: this.hubClient.hubOwner.id,
        interface: this.hubInterface,
        context: this.context,
        type: this.type,
        operation: create,
        commit_strategy: this.commitStrategy,
      },
      payload
    });

    this.hubClient.commit(commit);
  }
  
  public async getItems() {

    throw new UserAgentError('Not Implemented');
  }

  public async updateItem() {
    throw new UserAgentError('Not Implemented');
  }

  public async deleteItem() {
    throw new UserAgentError('Not Implemented');
  }
}