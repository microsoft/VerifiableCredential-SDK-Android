import HubRequest from './HubRequest';

/**
 * Optional filters used when querying the objects in an Identity Hub.
 */
export interface IObjectQueryOptions {

  /** Queries objects with the specified interface (e.g. `Collections` or `Actions`). */
  interface: string;

  /** Queries objects with the specified context (e.g. `schema.org`). */
  context?: string;

  /** Queries objects with the specified type (e.g. `MusicPlaylist`). */
  type?: string;

  /** Queries objects with the specified object ID. This filter is exclusive of all others. */
  object_id?: string[];

}

/**
 * Represents a request to a Hub to query the available objects.
 */
export default class HubObjectQueryRequest extends HubRequest {

  // Needed for correctly determining type of HubSession#send(), to ensure
  // the different request classes aren't structurally compatible.
  private readonly _isObjectQueryRequest = true;

  constructor(queryOptions: any) {
    super('ObjectQueryRequest', {
      query: queryOptions,
    });
  }

}
