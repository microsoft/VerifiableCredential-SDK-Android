import * as objectAssign from 'object-assign';

/**
 * The base class for all requests to an Identity Hub.
 */
export default class HubRequest {

  private requestType: string;
  private requestBody: any;

  constructor(requestType: string, requestBody?: any) {
    this.requestType = requestType;
    this.requestBody = requestBody;
  }

  /**
   * Returns the raw request JSON which will be sent to the Hub.
   */
  public async getRequestJson() {
    return objectAssign(
      {
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': this.requestType,
      },
      this.requestBody,
    );
  }

}
