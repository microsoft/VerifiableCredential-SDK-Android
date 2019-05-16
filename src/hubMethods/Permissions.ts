/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubMethods from './HubMethods';
import HubSession from '../hubSession/HubSession';
import CommitSigner from '../hubSession/crypto/CommitSigner';

/**
* A Class that does CRUD operations for storing items as Permissions in the Hub
*/
export default class Permissions extends HubMethods {

  constructor (hubSession: HubSession, commitSigner: CommitSigner) {
    super(hubSession, commitSigner, 'Permissions');
  }
}