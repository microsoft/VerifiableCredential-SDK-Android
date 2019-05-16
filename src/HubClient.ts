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
import CommitSigner from './hubSession/crypto/CommitSigner';

/**
 * Interface defining options for the
 * HubClient, such as hub Identifier and client Identifier.
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
  public readonly collections: Collections;

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
  constructor (_hubClientOptions: HubClientOptions) {
    // NEED to create a hubSession here.
    const hubSession = <HubSession> {};
    const commitSigner = <CommitSigner> {}
    this.actions = new Actions(hubSession, commitSigner);
    this.collections = new Collections(hubSession, commitSigner);
    this.permissions = new Permissions(hubSession, commitSigner);
    this.profile = new Profile(hubSession, commitSigner);
  }
}