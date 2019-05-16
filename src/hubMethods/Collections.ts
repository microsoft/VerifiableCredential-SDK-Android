/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubMethods from './HubMethods';
import UserAgentError from '../UserAgentError';
import HubSession from '../hubSession/HubSession';

/**
* A Class that does CRUD operations for storing items as Collections in the Hub
*/
export default class Collections extends HubMethods {

  constructor (hubSession: HubSession, type: string) {
    super(hubSession);
    throw new UserAgentError(`not yet implemented for type: '${type}`);
  }
}