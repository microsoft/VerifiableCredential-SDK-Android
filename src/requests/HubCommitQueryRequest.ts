import HubRequest from './HubRequest';

/**
 * Options which can be used when querying a Hub for commits.
 */
export interface ICommitQueryOptions {

  /** Used to request the constituent commits for one or more specific object IDs. */
  object_id?: string[];

  /** Used to request one or more specific commits based on their revision hash. */
  revision?: string[];

  /** Used to retrieve the next page of results for a previously-issued query. */
  skip_token?: string;

}

/**
 * Represents a request to a Hub for a set of commits.
 */
export default class HubCommitQueryRequest extends HubRequest {

  // Needed for correctly determining type of HubSession#send(), to ensure
  // the different request classes aren't structurally compatible.
  private readonly _isCommitQueryRequest = true;

  constructor(queryOptions: ICommitQueryOptions) {
    super('CommitQueryRequest', {
      query: queryOptions,
    });
  }

}
