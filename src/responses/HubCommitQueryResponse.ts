import { IHubCommitQueryResponse } from '@decentralized-identity/hub-common-js';
import SignedCommit from '../SignedCommit';

/**
 * Represents the response to a `HubCommitQueryRequest`.
 */
export default class HubCommitQueryResponse {

  constructor(private response: IHubCommitQueryResponse) {
    if (response['@type'] !== 'CommitQueryResponse') {
      throw new Error('Unexpected response type; expected CommitQueryResponse');
    }
  }

  /**
   * Returns the set of commits returned by the Hub.
   */
  public getCommits() {
    return this.response.commits.map((commit) => {
      return new SignedCommit(commit);
    });
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
