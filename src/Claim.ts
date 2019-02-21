import { ClaimObj } from './types';

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * Class for creating and managing a claim
 */
export default class Claim {

  /**
   * claim as a JWT
   */
  public jwt: string;

  /**
   * the ui references for the claim
   */
  public uiRef: any | undefined;

  /**
   * Constructs an instance of the Claim
   * class using the provided jwt or claimObj
   */
  constructor (claim: ClaimObj | string) {

    if (typeof claim === 'string') {
      this.jwt = claim;
    } else {
      this.jwt = claim.jwt;
      this.uiRef = claim.uiRef;
    }
  }

  /**
   * Creates a new claim object
   * @param options
   */
  public static create (jwt: string, uiRef: any) {
    const claim = new Claim({ jwt, uiRef });
    return claim;
  }

  /**
   * Get the uiRef for the claim
   */
  public getClaimUI () {
    if (!this.uiRef) {
      throw new Error('No UI Reference for Claim');
    }
    return this.uiRef;
  }

}
