/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { ServiceReference } from './types';
import { Identifier } from '.';

/**
 * Class for creating, managing, and storing claims,
 */
export default class Claims {

  /**
   * The identifier that owns the claims
   */
  public identifier: Identifier;

  /**
   * An Array of Claim objects owned by the identifier.
   * TODO: add a way to do just identifier or both and way to create hub instance or something
   */
  // public hubSession: HubSession;

  /**
   *
   */
  public serviceReferences: Array<ServiceReference> = [];

  constructor (identifier: Identifier) {

    this.identifier = identifier;

    if (identifier.document) {
      this.serviceReferences = identifier.document.serviceReferences;

      // const hubSessionOptions: HubSession = {
      //   hubEndpoint: this.serviceReferences[0].serviceEndpoint,
      //   hubDid: this.identifier.id,
      //   resolver: new HttpResolver()

      // };

      // this.hubSession = new HubSession({})
    }
  }

}
