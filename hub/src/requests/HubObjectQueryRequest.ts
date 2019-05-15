import { IHubObjectQueryOptions } from '@decentralized-identity/hub-common-js';
import HubRequest from './HubRequest';

/**
 * Represents a request to a Hub to query the available objects.
 */
export default class HubObjectQueryRequest extends HubRequest {

  // Needed for correctly determining type of HubSession#send(), to ensure
  // the different request classes aren't structurally compatible.
  private readonly _isObjectQueryRequest = true;

  constructor(queryOptions: IHubObjectQueryOptions) {
    super('ObjectQueryRequest', {
      query: queryOptions,
    });
  }

}
