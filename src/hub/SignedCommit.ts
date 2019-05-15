import { ICommitProtectedHeaders, IFlattenedJws } from '@decentralized-identity/hub-common-js';
import base64url from 'base64url';
import * as crypto from 'crypto';

/**
 * Class representing a signed commit.
 */
export default class SignedCommit {

  constructor(private json: IFlattenedJws) {

  }

  /**
   * Returns the signed commit data in the Flattened JWS JSON Serialization.
   */
  toFlattenedJson(): IFlattenedJws {
    return this.json;
  }

  /**
   * Returns the decoded protected headers for this commit. TODO TODO NO REV
   */
  getProtectedHeaders(): ICommitProtectedHeaders {
    if (this.json && this.json.protected) {
      return JSON.parse(base64url.decode(this.json.protected));
    }

    throw new Error('Commit does not have a protected field.');
  }

  /**
   * Returns the decoded payload for this commit.
   */
  getPayload(): any {
    if (this.json && this.json.payload) {
      const decoded = base64url.decode(this.json.payload);
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
    // TODO: Verify signature; cache result
    const sha256 = crypto.createHash('sha256');
    sha256.update(`${this.json.protected}.${this.json.payload}`);
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
