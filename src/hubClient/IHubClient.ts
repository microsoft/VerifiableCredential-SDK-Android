/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import Commit from '../hubSession/Commit';
import HubObjectQueryRequest from '../hubSession/requests/HubObjectQueryRequest';

/**
 * Class for defining options for the
 * HubClient, such as hub Identifier and client Identifier.
 */
export class HubClientOptions {
  /**
   * The Identifier of the owner of the hub.
   */
  hubOwner: Identifier | undefined;

  /**
   * The Identifier of the Client that wants to start hub session.
   */
  clientIdentifier: Identifier | undefined;

  /**
   * Key reference to private key to be used to sign commits and create HubSession
   */
  keyReference: string | undefined;
}

/**
 * Interface for HubClient class that manages which hub instance to create hub session with
 * And commits and queries for objects in the hub session.
 */
export default interface IHubClient {

  /**
   * Signs and sends a commit to the hub owner's hub.
   * @param commit 
   */
  commit (commit: Commit): Promise<void>;

  /**
   * Query Objects of certain type in Hub.
   * @param queryRequest object that tells the hub what objects to get.
   */
  queryObjects (queryRequest: HubObjectQueryRequest): Promise<any>;

}