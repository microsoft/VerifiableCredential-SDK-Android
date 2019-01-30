/**
 * Represents the properties returned by an Identity Hub in all responses.
 */
export default interface IHubResponse {

  /** The constant schema for all Hub responses. */
  '@context': 'https://schema.identity.foundation/0.1';

  /** The specific type of the response. */
  '@type': string;

}
