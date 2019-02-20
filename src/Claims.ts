/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Persona from './Persona';
import { Claim } from './types';

/**
 * Class for creating, managing, and storing claims,
 */
export default class Claims {

  /**
   * The persona that owns the claims.
   */
  public persona: Persona;

  /**
   * An Array of Claim objects owned by the persona.
   * TODO: add a way to do just identifier or both and way to create hub instance or something
   */
  public claims: Array<Claim> = [];

  constructor (persona: Persona) {

    this.persona = persona;
  }

  /**
   * Add a claim to the Claims object
   * @param claim claim object to be added to collection
   */
  public addClaim (claim: Claim) {
    this.claims.push(claim);
  }

  /**
   *
   */

}
