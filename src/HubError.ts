import IHubError from './interfaces/IHubError';

/**
 * List of error codes potentially returned by an Identity Hub.
 */
export enum HubErrorCode {

  /** Indicates that the request issued by the client was invalid. */
  BadRequest = 'bad_request',

  /** Indicates that the attempted authentication method was invalid or expired. */
  AuthenticationFailed = 'authentication_failed',

  /** Indicates that the client lacks necessary permissions to complete the request. */
  PermissionsRequired = 'permissions_required',

  /** Indicates that the requested entity was not found. */
  NotFound = 'not_found',

  /** Indicates that the client has made too many requests recently and should back off attempts. */
  TooManyRequests = 'too_many_requests',

  /** Indicates that an internal error occurred inside the Hub. */
  ServerError = 'server_error',

  /** Indicates that the requested interface/method is not yet implemented. */
  NotImplemented = 'not_implemented',

  /** Indicates that the Hub service is temporarialy unavailable. */
  ServiceUnavailable = 'service_unavailable',

  /** Indicates that the Hub service is temporarialy unavailable. */
  TemporarilyUnavailable = 'temporarily_unavailable',

}

/**
 * Represents an error returned by an Identity Hub.
 */
export default class HubError extends Error {

  // tslint:disable-next-line:variable-name
  private __hubError = true;

  /**
   * Indicates whether the passed-in object is a HubError instance.
   */
  public static is(err: any): err is HubError {
    return err.__hubError || false;
  }

  constructor (private body: IHubError) {
    super(`Identity Hub Error: ${body.developer_message || body.error_code || 'Unknown error'}`);

    // NOTE: Extending 'Error' breaks prototype chain since TypeScript 2.1.
    // The following line restores prototype chain.
    if ((Object as any).setPrototypeOf) (Object as any).setPrototypeOf(this, new.target.prototype);
  }

  /**
   * Returns the error code given by the Hub.
   */
  public getErrorCode() {
    return this.body.error_code;
  }

  /**
   * Returns the error target (e.g. the property which is invalid).
   */
  public getTarget() {
    return this.body.target;
  }

  /**
   * Returns the raw error JSON as provided by the Hub.
   */
  public getRawError() {
    return this.body;
  }

}
