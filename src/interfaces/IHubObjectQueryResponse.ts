import IHubResponse from './IHubResponse';
import IObjectMetadata from './IObjectMetadata';

/**
 * Represents a Hub's response to an `ObjectQueryRequest`.
 */
export default interface IHubObjectQueryResponse extends IHubResponse {

  /** The constant schema for all Hub responses. */
  '@context': 'https://schema.identity.foundation/0.1';

  /** The constant type of this response. */
  '@type': 'ObjectQueryResponse';

  /** Array containing the requested object metadata. */
  'objects': IObjectMetadata[];

  /** The pagination token which can be used to fetch the next page of results. */
  'skip_token'?: string;

}
