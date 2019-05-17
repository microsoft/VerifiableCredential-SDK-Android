/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import HubSession from '../hubSession/HubSession';
import CommitSigner from '../hubSession/crypto/CommitSigner';

/**
 * Constants that represent what type of commit strategy to be used.
 */
export enum CommitStrategyReference {
  Basic = 'basic',
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
   * HubSession used to send Hub Requests to the Hub.
   */
  hubSession: HubSession;

  /**
   * CommitSigner object to sign the commits
   */
  commitSigner: CommitSigner;

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

  private hubSession: HubSession; 
  private hubInterface: HubInterface;
  private commitSigner: CommitSigner;
  private commitStrategy: CommitStrategyReference;

  /**
   * Creates an instance of HubMethods that will be used to send hub requests and responses.
   * @param [hubMethodOptions] for configuring how to form hub requests and responses.
   */
  constructor (hubMethodOptions: HubMethodsOptions) {

    this.hubSession = hubMethodOptions.hubSession;   
    this.commitSigner = hubMethodOptions.commitSigner;

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

  public async getItem() {

    throw new UserAgentError('Not Implemented');
  }

  public async updateItem() {
    throw new UserAgentError('Not Implemented');
  }

  public async addItem() {
    throw new UserAgentError('Not Implemented');
  }

  public async deleteItem() {
    throw new UserAgentError('Not Implemented');
  }
}