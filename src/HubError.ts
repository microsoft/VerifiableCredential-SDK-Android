import { IHubError, HubErrorCode } from '@decentralized-identity/hub-common-js';

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
  public getErrorCode(): HubErrorCode {
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
