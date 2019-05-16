/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Actions from './hubMethods/Actions';
import Collections from './hubMethods/Collections';
import Permissions from './hubMethods/Permissions';
import Profile from './hubMethods/Profile';
import Identifier from './Identifier';
import HubSession from './hubSession/HubSession';

/**
 * Interface defining options for the
 * Hub Client, such as hub Identifier and client Identifier.
 */
export interface HubClientOptions {
  /**
   * The Identifier of the Hub that client wants to access.
   */
  hubIdentifier: Identifier;

  /**
   * The Identifier of the Client that wants to start hub session.
   */
  clientIdentifier: Identifier;

  /**
   * The specific type of object that will be store in Collections.
   */
  collectionsType?: string;
}

/**
 * Class for doing CRUD operations to Actions, Collections, Permissions, and Profile
 * In a Hub.
 */
export default class HubClient {

  /**
   * Actions Object for adding, updating, deleting, reading actions to hub
   */
  public readonly actions: Actions;

  /**
   * Collections Object for adding, updating, deleting, reading collections to hub
   */
  public readonly collections: Collections | undefined;

  /**
   * Permission Object for adding, updating, deleting, reading permissions to hub
   */
  public readonly permissions: Permissions;

  /**
   * Profile Object for adding, updating, deleting, reading profile to hub
   */
  public readonly profile: Profile;

  /**
   * Constructs an instance of the Hub Client Class for hub operations
   * @param hubClientOptions hub client options used to create instance.
   */
  constructor (hubClientOptions: HubClientOptions) {
    const hubSession = <HubSession> {};
    this.actions = new Actions(hubSession);
    if (hubClientOptions.collectionsType) {
      this.collections = new Collections(hubSession, hubClientOptions.collectionsType);
    }
    this.permissions = new Permissions(hubSession);
    this.profile = new Profile(hubSession);
  }
}