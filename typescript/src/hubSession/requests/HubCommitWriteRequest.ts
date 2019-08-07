/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubRequest from './HubRequest';
import { ICryptoToken } from '../../crypto/protocols/ICryptoToken';
import JoseConstants from '../../crypto/protocols/jose/JoseConstants';
import base64url from 'base64url';
import { TSMap } from 'typescript-map';

/**
 * Represents a request to commit the given Commit object to an Identity Hub.
 */
export default class HubCommitWriteRequest extends HubRequest {

  // Needed for correctly determining type of HubSession#send(), to ensure
  // the different request classes aren't structurally compatible.
  private readonly _isWriteRequest = true;

  constructor(commit: ICryptoToken) {
    super('WriteRequest', {
      // fix to match format used in hub. needs to be normalized - todo
      commit: commit.serialize()});
    }

    private static getProtected(protectedHeader: TSMap<string, string>, header: TSMap<string, string>) {
      if (!header) {
        return '';
      }
      header.forEach((value: any, key: any) => {
        protectedHeader.set(key, value);
      });
      return base64url.encode(JSON.stringify(protectedHeader.toJSON()));
    }
}
