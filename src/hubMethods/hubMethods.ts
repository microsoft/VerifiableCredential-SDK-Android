/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserAgentError from '../UserAgentError';
import HubSession from '../hubSession/HubSession';

/**
* An Abstract Class for HubMethods.
*/
export default abstract class HubMethods {

  private hubSession: HubSession; 

  constructor (hubSession: HubSession) {
    this.hubSession = hubSession;
  }

  public getItem() {
    throw new UserAgentError('Not Implemented');
  }

  public updateItem() {
    throw new UserAgentError('Not Implemented');
  }

  public addItem() {
    throw new UserAgentError('Not Implemented');
  }

  public deleteItem() {
    throw new UserAgentError('Not Implemented');
  }
}