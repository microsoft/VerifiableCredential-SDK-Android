/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import * as crypto from 'crypto';
import { ICryptoToken } from '../crypto/protocols/ICryptoToken';
import JoseConstants from '../crypto/protocols/jose/JoseConstants';
import { ICommitProtectedHeaders } from '@decentralized-identity/hub-common-js';

/**
 * Class representing a protected commit.
 */
export default class ProtectedCommit {

  constructor(private json: ICryptoToken) {

  }

  /**
   * Returns the protected commit data in the Flattened JWS JSON Serialization.
   */
  toFlattenedJson(): ICryptoToken {
    return this.json;
  }

  /**
   * Returns the decoded protected headers for this commit.
   */
  getProtectedHeaders(): ICommitProtectedHeaders {
    if (this.json && this.json.get(JoseConstants.tokenProtected)) {
      return JSON.parse(base64url.decode(this.json.get(JoseConstants.tokenProtected)));
    }

    throw new Error('Commit does not have a protected field.');
  }

  /**
   * Returns the decoded payload for this commit.
   */
  getPayload(): any {
    if (this.json && this.json.get(JoseConstants.tokenPayload)) {
      const decoded = base64url.decode(this.json.get(JoseConstants.tokenPayload).toString());
      try {
        return JSON.parse(decoded);
      } catch (e) {
        // Not JSON, return directly
        return decoded;
      }
    }

    throw new Error('Commit does not have a payload field.');
  }

  /**
   * Retrieves the revision ID for this commit.
   */
  getRevision(): string {
    // NEED: Verify signature; cache result
    const sha256 = crypto.createHash('sha256');
    sha256.update(`${this.json.get(JoseConstants.tokenProtected)}.${this.json.get(JoseConstants.tokenPayload)}`);
    return sha256.digest('hex');
  }

  /**
   * Retrieves the ID of the object to which this commit belongs.
   */
  getObjectId(): string {
    const headers = this.getProtectedHeaders();

    if (headers.operation === 'create') {
      return this.getRevision();
    }

    return headers.object_id!;
  }

}
