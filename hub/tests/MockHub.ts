import { PrivateKey, Authentication, VerifiedRequest } from "@decentralized-identity/did-auth-jose";
import { IDidResolver } from "@decentralized-identity/did-common-typescript";
import { Response, Request } from 'node-fetch';

/** Handler to intercept requests before they are authenticated. */
type MockHubPreAuthHandler = (body: Buffer) => Promise<Response | undefined>;

interface MockHubHandlerAuthRequestParameters {
  isAuthTokenRequest: true;
  authTokenResponse: Response;
}

interface MockHubHandlerClientRequestParameters {
  isAuthTokenRequest: false;
  clientRequest: VerifiedRequest;
}

type MockHubHandlerParameters = MockHubHandlerAuthRequestParameters | MockHubHandlerClientRequestParameters;

/** Handler to intercept requests after they are authenticated. */
type MockHubHandler = (params: MockHubHandlerParameters) => Promise<Response | string>;

interface MockHubOptions {
  hubDid: string;
  hubPrivateKey: PrivateKey;
  resolver: IDidResolver;
}

/**
 * Mock Hub implementation for testing requests/responses.
 *
 * This class handles the authentication/encryption wrapping and unwrapping, and calls a provided
 * handler function to decide on the actual response.
 */
export default class MockHub {

  private authentication: Authentication;

  private preAuthHandler: MockHubPreAuthHandler | undefined;
  private handler: MockHubHandler | undefined;

  constructor(options: MockHubOptions) {

    this.authentication = new Authentication({
      resolver: options.resolver,
      keys: {
        [options.hubPrivateKey.kid]: options.hubPrivateKey
      }
    });

  }

  /**
   * Configures a test handler callback which will be called before the incoming request is
   * validated. Return a Buffer to short-circuit the response; or undefined to continue processing
   * the request normally.
   */
  async setPreAuthHandler(handler: MockHubPreAuthHandler) {
    this.preAuthHandler = handler;
  }

  /**
   * Configures a test handler callback which will be called after the incoming request is
   * validated. This callback plays the role of the Hub and decides how to respond.
   */
  async setHandler(handler: MockHubHandler) {
    this.handler = handler;
  }

  /**
   * Handles an intercepted call to fetch() by processing the request and calling the configured
   * mock callback to handle the response.
   */
  async handleFetch(_: string | Request, init?: RequestInit): Promise<Response> {

    if (!init) throw new Error('MockHub: The RequestInit fetch parameter was not present.');
    if (!Buffer.isBuffer(init.body)) throw new Error('MockHub: The request body was not a Buffer.');

    if (this.preAuthHandler) {
      const preAuthResponse = await this.preAuthHandler(init.body);
      if (preAuthResponse) return preAuthResponse;
    }

    let verifiedRequest = await this.authentication.getVerifiedRequest(init.body);

    // let isAuthTokenRequest = Buffer.isBuffer(verifiedRequest);

    let handlerParameters: MockHubHandlerParameters;

    if (Buffer.isBuffer(verifiedRequest)) {
      // Auth token request
      handlerParameters = {
        isAuthTokenRequest: true,
        authTokenResponse: new Response(verifiedRequest)
      } as MockHubHandlerAuthRequestParameters;
    } else {
      // Client request
      handlerParameters = {
        isAuthTokenRequest: false,
        clientRequest: verifiedRequest
      } as MockHubHandlerClientRequestParameters;
    }

    if (!this.handler) {
      throw new Error('MockHub: Handler not set.');
    }

    let handlerResponse = await this.handler(handlerParameters);

    if (typeof handlerResponse === 'string') {
      // Returned a real response
      let responseBuffer = await this.authentication.getAuthenticatedResponse(verifiedRequest as VerifiedRequest, handlerResponse);
      return new Response(responseBuffer);
    }

    // handlerResponse instanceof Response
    return handlerResponse;
  }

}