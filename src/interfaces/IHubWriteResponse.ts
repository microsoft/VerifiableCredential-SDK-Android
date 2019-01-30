import IHubResponse from './IHubResponse';

/**
 * Represents a Hub's response to an `WriteRequest`.
 */
export default interface IHubWriteResponse extends IHubResponse {

  /** The constant schema for all Hub responses. */
  '@context': 'https://schema.identity.foundation/0.1';

  /** The constant type of this response. */
  '@type': 'WriteResponse';

  /** The list of known revisions for the object which was created/modified. */
  'revisions': string[];

}
