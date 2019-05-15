import { IHubObjectQueryResponse } from '@decentralized-identity/hub-common-js';

/**
 * Represents the response to a `HubObjectQueryRequest`.
 */
export default class HubObjectQueryResponse {

  private response: IHubObjectQueryResponse;

  constructor(json: IHubObjectQueryResponse) {
    if (json['@type'] !== 'ObjectQueryResponse') {
      throw new Error('Unexpected response type; expected ObjectQueryResponse');
    }

    this.response = json;
  }

  /**
   * Returns the set of objects returned by the Hub.
   *
   * TODO: Map JSON into useful objects, as done for commits.
   */
  public getObjects() {
    return this.response.objects || [];
  }

  /**
   * Indicates whether additional pages of results are available.
   */
  public hasSkipToken() {
    return !!this.response.skip_token;
  }

  /**
   * Retrieves a token which can be used to fetch subsequent result pages.
   */
  public getSkipToken() {
    return this.response.skip_token;
  }

}
