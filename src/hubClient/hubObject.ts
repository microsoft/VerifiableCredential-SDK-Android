/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { IObjectMetadata } from '@decentralized-identity/hub-common-js';
import UserAgentError from '../UserAgentError';
import HubCommitQueryRequest from '../hubSession/requests/HubCommitQueryRequest';
import CommitStrategyBasic from '../hubSession/CommitStrategyBasic';
import HubSession from '../hubSession/HubSession';

/**
 * Class that represents an object in a hub.
 */
export default class HubObject {
  
  /**
   * The actual payload of the object.
   */
  public payload: any | undefined;

  /**
   * Object Metadata that can be used to query for the actual object in the hub.
   */
  public readonly objectMetadata: IObjectMetadata;

  /**
   * Create an instance for Hub Object using hub object's metadata.
   * @param objectMetadata object metadata that represents an object in a hub.
   */
  constructor (objectMetadata: IObjectMetadata) {
    this.objectMetadata = objectMetadata;
  }

  /**
   * If payload is not defined, get the payload from hub session using metadata.
   * @param hubSession the hub session that will be used to query object
   * @param commitQueryRequest the commit query requests for getting all commits for certain object.
   */
  public async hydrate(hubSession: HubSession, commitQueryRequest: HubCommitQueryRequest): Promise<any> {
    if (this.payload) {
      return this.payload;
    }
    if (this.objectMetadata.commit_strategy !== 'basic') {
      throw new UserAgentError('Currently only the basic commit strategy is supported.');
    }
    const commitQueryResponse = await hubSession.send(commitQueryRequest);
    const commits = commitQueryResponse.getCommits();
    const strategy = new CommitStrategyBasic();
    this.payload = await strategy.resolveObject(commits);
    return this.payload;
  }

  /**
   * 
   */
  public getPayload() {
    if (this.payload) {
      return this.payload;
    }
    throw new UserAgentError(`payload undefined for '${this.objectMetadata.id}'`);
    
  }

  public getMetadata(): IObjectMetadata {
    return this.objectMetadata;
  }
}
