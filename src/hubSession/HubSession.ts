/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { IHubError, IHubResponse, HubErrorCode } from '@decentralized-identity/hub-common-js';
import HubRequest from './requests/HubRequest';
import HubError from './HubError';
import HubCommitWriteRequest from './requests/HubCommitWriteRequest';
import HubWriteResponse from './responses/HubWriteResponse';
import HubObjectQueryRequest from './requests/HubObjectQueryRequest';
import HubObjectQueryResponse from './responses/HubObjectQueryResponse';
import HubCommitQueryRequest from './requests/HubCommitQueryRequest';
import HubCommitQueryResponse from './responses/HubCommitQueryResponse';

// tslint:disable-next-line:import-name
import fetch, { Request, RequestInit } from 'node-fetch';
import Identifier from '../Identifier';

/**
 * Options for instantiating a new Hub session.
 */
export interface HubSessionOptions {

  /** 
   * The DID of the client, i.e the identity of the user/app using this SDK. 
   */
  client: Identifier;

  /**
   * The private key to use for decrypting/signing when communicating with the Hub. Must be
   * registered in the DID document of the clientDid.
   */
  clientPrivateKeyReference: string;

  /** 
   * The Identfier of the owner of the Hub with which we will be communicating. 
   */
  hubOwner: Identifier;

  /** 
   * The Identifier of the Hub, for addressing request envelopes. 
   */
  hub: Identifier;

  /** 
   * The HTTPS endpoint of the Hub. 
   */
  hubEndpoint: string;
}

/**
 * Represents a communication session with a particular Hub instance.
 */
export default class HubSession {

  private client: Identifier;
  private hub: Identifier;
  private hubEndpoint: string;
  private hubOwner: Identifier;
  private currentAccessToken: string | undefined;

  constructor(options: HubSessionOptions) {
    this.client = options.client;
    this.hub = options.hub;
    this.hubEndpoint = options.hubEndpoint;
    this.hubOwner = options.hubOwner;
  }

  /**
   * Sends the given request to the Hub instance, and returns the associated response.
   *
   * @param request An instance or subclass of HubRequest to be sent.
   */
  send(request: HubCommitWriteRequest): Promise<HubWriteResponse>;
  send(request: HubObjectQueryRequest): Promise<HubObjectQueryResponse>;
  send(request: HubCommitQueryRequest): Promise<HubCommitQueryResponse>;
  async send(request: HubRequest): Promise<any> {

    const rawRequestJson = await request.getRequestJson();

    rawRequestJson.iss = this.client.id;
    rawRequestJson.aud = this.hub.id;
    rawRequestJson.sub = this.hubOwner.id;

    const rawRequestString = JSON.stringify(rawRequestJson);
    const accessToken = await this.getAccessToken();

    let responseString: string;

    try {
      responseString = await this.makeRequest(rawRequestString, accessToken);
    } catch (e) {
      // If the access token has expired, renew access token and retry
      if (HubError.is(e) && e.getErrorCode() === HubErrorCode.AuthenticationFailed) {
        const newAccessToken = await this.refreshAccessToken();
        responseString = await this.makeRequest(rawRequestString, newAccessToken);
      } else {
        throw e;
      }
    }

    let responseObject: IHubResponse<string>;

    try {
      responseObject = JSON.parse(responseString);
    } catch (e) {
      throw new HubError({
        error_code: HubErrorCode.ServerError,
        developer_message: `Unexpected error decoding JSON response: ${e.message}`,
        inner_error: e,
      });
    }

    return HubSession.mapResponseToObject(responseObject);
  }

  /**
   * Sends a raw (string) request body to the Hub and receives a response.
   *
   * @param message The raw request body to send.
   * @param accessToken The access token to include in the request, if any.
   */
  private async makeRequest(message: string, accessToken?: string): Promise<string> {

    const requestBuffer = undefined;  
    // await this.authentication.getAuthenticatedRequest(message, this.hubDid, accessToken);
  
    const res = await this.callFetch(this.hubEndpoint, {
      method: 'POST',
      body: requestBuffer,
      headers: {
        'Content-Type': 'application/jwt',
        'Content-Length': '1' // requestBuffer.length.toString(),
      },
    });

    if (res.status !== 200) {
      const errorResponse = await res.json();
      throw new HubError(errorResponse);
    }

    const response = await res.buffer();
    const plainResponse = {request: ''}; // await this.authentication.getVerifiedRequest(response, false);
    if (plainResponse instanceof Buffer) {
      // This should never happen as it means we are trying to return an access token in response
      throw new Error('Internal error during decryption.');
    }

    return plainResponse.request;
  }

  /**
   * Fetch API wrapper, to allow unit testing.
   *
   * @param url The URL to make a request to.
   * @param init Request initialization details.
   */
  private async callFetch(url: string | Request, init?: RequestInit) {
    return fetch(url, init);
  }

  /**
   * Returns the current access token for the Hub, requesting one if necessary.
   */
  private async getAccessToken(): Promise<string> {
    if (this.currentAccessToken) {
      return this.currentAccessToken;
    }

    return this.refreshAccessToken();
  }

  /**
   * Requests an updated access token from the Hub.
   */
  private async refreshAccessToken(): Promise<string> {
    this.currentAccessToken = await this.makeRequest('');
    return this.currentAccessToken!;
  }

  /** 
   * Mapping of known response types. 
   */
  private static responseTypes = {
    WriteResponse: HubWriteResponse,
    ObjectQueryResponse: HubObjectQueryResponse,
    CommitQueryResponse: HubCommitQueryResponse,
  };

  /**
   * Transforms a JSON blob returned by the Hub into a subclass of HubResponse, based on the `@type`
   * field of the response.
   *
   * @param response The Hub response to be transformed.
   */
  private static mapResponseToObject(response: IHubResponse<string>) {
    const responseTypeString = response['@type'];
    const responseType = (<any> HubSession.responseTypes)[responseTypeString];

    if (responseType) {
      return new responseType(response);
    }

    if (response['@type'] === 'ErrorResponse') {
      throw new HubError(<IHubError> <any> response);
    }

    throw new HubError({
      error_code: HubErrorCode.NotImplemented,
      developer_message: `Unexpected response type ${responseTypeString}`,
    });
  }

}
