/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from '../Identifier';
import Commit from '../hubSession/Commit';
import HubObjectQueryRequest from '../hubSession/requests/HubObjectQueryRequest';
import HubCommitQueryRequest from '../hubSession/requests/HubCommitQueryRequest';
import ProtectionStrategy from '../crypto/strategies/ProtectionStrategy';
import { CryptoOptions, HubObject, PublicKey } from '..';

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
   * The public keys used for encrypting the payload
   */
  recipientsPublicKeys: PublicKey[] | undefined;
 
  /**
   * Defines the protection strategy that will be used to protect the commit.
   */
  hubProtectionStrategy: ProtectionStrategy | undefined;
  
  /**
   * Crypto Options
   * contains algorithm and other data about crypto
   */
   public cryptoOptions: CryptoOptions = new CryptoOptions();
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
  commit (commit: Commit): Promise<any>;

   /**
   * Query Objects of certain type in Hub.
   * @param queryRequest object that tells the hub what object to get.
   */
  queryObjects (queryRequest: HubObjectQueryRequest): Promise<HubObject[]>

    /**
   * Query Object specified by certain id 
   * @param commitQueryRequest HubCommitQueryRequest object to request object of specific id.
   * @param hubObject a HubObject containing metadata such as object id.
   */
  queryObject (commitQueryRequest: HubCommitQueryRequest, hubObject: HubObject): Promise<HubObject> 

}
