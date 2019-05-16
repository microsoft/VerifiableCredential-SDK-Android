/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubMethods from './HubMethods';
import HubSession from '../hubSession/HubSession';
import CommitSigner from '../hubSession/crypto/CommitSigner';

/**
* A Class that represents objects 
*/
export default class Actions extends HubMethods {

  constructor (hubSession: HubSession, commitSigner: CommitSigner) {
    super(hubSession, commitSigner, 'Actions');
  }
}
