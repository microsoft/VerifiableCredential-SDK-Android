import IHubWriteResponse from '../interfaces/IHubWriteResponse';

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
