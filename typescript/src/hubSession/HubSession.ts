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
import nodeFetch, { Request, RequestInit } from 'node-fetch';
import Identifier from '../Identifier';
import DidProtocol, { DidProtocolOptions } from '../crypto/protocols/did/DidProtocol';
import HttpResolver from '../resolvers/HttpResolver';
import IResolver from '../resolvers/IResolver';
import UserAgentError from '../UserAgentError';

/**
 * Options for instantiating a new Hub session.
 */
export class HubSessionOptions {

  /** 
   * The DID of the client, i.e the identity of the user/app using this SDK. 
   */
  client?: Identifier;

  /**
   * The reference to the private key to use for signing when communicating with the Hub. Must be
   * registered in the DID document of the clientDid.
   */
  signingKeyReference: string | undefined;

  /**
   * The reference to the private key to use for decrypting the hub communication. Must be
   * registered in the DID document of the clientDid.
   */
  encryptionKeyReference: string | undefined;

  /** 
   * The Identfier of the owner of the Hub with which we will be communicating. 
   */
  hubOwner?: Identifier;

  /** 
   * The Identifier of the Hub, for addressing request envelopes. 
   */
  hubId: string = 'did:test:hub.id';

  /** 
   * The HTTPS endpoint of the Hub. 
   */
  hubEndpoint: string = 'https://beta.hub.microsoft.com';
}

/**
 * Represents a communication session with a particular Hub instance.
 */
export default class HubSession {

  private client?: Identifier;
  private hubId: string;
  private hubEndpoint: string;
  private hubOwner?: Identifier;
  private signingKeyReference: string | undefined;
  private encryptionKeyReference: string | undefined;

  constructor(options: HubSessionOptions) {
    this.client = options.client;
    this.hubId = options.hubId;
    this.hubEndpoint = options.hubEndpoint;
    this.hubOwner = options.hubOwner;
    this.signingKeyReference = options.signingKeyReference;
    this.encryptionKeyReference = options.encryptionKeyReference;
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

    if (!this.client || !this.hubOwner) {
      throw new HubError({
        error_code: HubErrorCode.NotFound,
        developer_message: `Identifiers not Specified`,
      });
    }

    rawRequestJson.iss = this.client.id;
    rawRequestJson.aud = this.hubId;
    rawRequestJson.sub = this.hubOwner.id;

    //rawRequestJson.commit = rawRequestJson.commit.serialize();
    const rawRequestString = JSON.stringify(rawRequestJson);
    const response = await this.makeRequest(rawRequestString);
    let responseObject: IHubResponse<string>;

    try {
      responseObject = response;
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
  private async makeRequest(message: string): Promise<any> {

    if (!this.client || !this.hubOwner) {
      throw new HubError({
        error_code: HubErrorCode.AuthenticationFailed,
        developer_message: `Client Identifier not specified in options`,
      });
    }

    let resolver: IResolver;
    if (this.client.options && this.client.options.resolver) {
      resolver = this.client.options.resolver;
    } else {
      resolver = new HttpResolver('https://beta.discover.did.microsoft.com/');
    }

    // await this.authentication.getAuthenticatedRequest(message, this.hubDid, accessToken);
    const didProtocolOptions: DidProtocolOptions = {
      sender: this.client,
      resolver
    };
    const didProtocol = new DidProtocol(didProtocolOptions);
    if (!this.signingKeyReference) {
      throw new UserAgentError('Signing key reference is not defined');
    }

    const request = await didProtocol.signAndEncrypt(message, this.hubId);
  
    const res = await this.callFetch(this.hubEndpoint, {
      method: 'POST',
      body: request,
      headers: {
        'Content-Type': 'application/jwt',
        'Content-Length': request.length.toString()
      },
    });

    if (res.status !== 200) {
      const errorResponse = await res.json();
      throw new HubError(errorResponse);
    }

    const response = await res.buffer();
    return didProtocol.decryptAndVerify(<string>this.encryptionKeyReference, response);
  }

  /**
   * Fetch API wrapper, to allow unit testing.
   *
   * @param url The URL to make a request to.
   * @param init Request initialization details.
   */
  private async callFetch(url: string | Request, init?: RequestInit) {
    return nodeFetch(url, init);
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
