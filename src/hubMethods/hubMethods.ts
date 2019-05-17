/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import HubSession from '../hubSession/HubSession';
import CommitSigner from '../hubSession/crypto/CommitSigner';

/**
 * An Abstract Class for HubMethods.
 * 
 */
export default abstract class HubMethods {

  private hubSession: HubSession; 
  private method: string;
  private commitSigner: CommitSigner;

  constructor (hubSession: HubSession, commitSigner: CommitSigner, method: string) {
    this.hubSession = hubSession;
    this.commitSigner = commitSigner;
    this.method = method;
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