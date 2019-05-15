import { IHubCommitQueryOptions } from '@decentralized-identity/hub-common-js';
import HubRequest from './HubRequest';

/**
 * Represents a request to a Hub for a set of commits.
 */
export default class HubCommitQueryRequest extends HubRequest {

  // Needed for correctly determining type of HubSession#send(), to ensure
  // the different request classes aren't structurally compatible.
  private readonly _isCommitQueryRequest = true;

  constructor(queryOptions: IHubCommitQueryOptions) {
    super('CommitQueryRequest', {
      query: queryOptions,
    });
  }

}
