import { IHubWriteResponse } from '@decentralized-identity/hub-common-js';

/**
 * Represents the response to a `HubWriteRequest`.
 */
export default class HubWriteResponse {

  constructor (private response: IHubWriteResponse) {

  }

  /**
   * Returns the list of known revisions for the object which was created/modified.
   */
  public getRevisions() {
    return this.response.revisions;
  }

}
