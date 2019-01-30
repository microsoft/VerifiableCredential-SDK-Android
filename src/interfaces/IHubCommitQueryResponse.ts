import IHubResponse from './IHubResponse';
import IFlattenedJws from './IFlattenedJws';

/**
 * Represents a Hub's response to a `CommitQueryRequest`.
 */
export default interface IHubCommitQueryResponse extends IHubResponse {

  /** The constant schema for all Hub responses. */
  '@context': 'https://schema.identity.foundation/0.1';

  /** The constant type of this response. */
  '@type': 'CommitQueryResponse';

  /** Array containing the requested commits. */
  'commits': IFlattenedJws[];

  /** The pagination token which can be used to fetch the next page of results. */
  'skip_token'?: string;

}
