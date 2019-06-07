/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IHubClient from '../../src/hubClient/IHubClient';
import Commit from '../../src/hubSession/Commit';
import { HubCommitQueryRequest, HubObject } from '../../src';

export default class HubClientMock implements IHubClient {

  constructor(){}

  public async commit(commit: Commit) {
    console.log(commit);
    return commit;
  }

  public async queryObjects(obj: any) {
    return obj;
  }

  public async queryObject(queryRequest: any, hubObject: any) {
    console.log(queryRequest);
    return hubObject;
  }
}
