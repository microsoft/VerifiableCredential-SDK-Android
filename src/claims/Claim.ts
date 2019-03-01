/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { UriDescription } from '../types';
import { ClaimObject } from './models/ClaimObject';
import { ClaimClass } from './models/ClaimClass';

/**
 * Interface defining methods and properties for a Claim object.
 * The properties such as issuer, logo, name, and descriptions are what are meant to be rendered on the claim UI.
 */
export default class Claim {

  /**
   * The claim details present in the claim.
   * This includes the verifiable part of the claim.
   */
  public claimDetails: string;

  /**
   * Issuer Name
   */
  public issuer: string;

  /**
   * Issuer Logo
   */
  public logo: UriDescription;

  /**
   * Claim Name
   */
  public name: string;

  /**
   * Claim Descriptions
   */
  public descriptions: any;

  /**
   * Contructs an instance of the Claim class
   */
  constructor (claimObject: ClaimObject, claimClass: ClaimClass) {
    this.claimDetails = claimObject.claimDetails;
    this.issuer = claimClass.issuerName;
    this.logo = claimClass.claimLogo.sourceUri;
    this.name = claimClass.claimName;
    this.descriptions = claimClass.claimDescriptions;
  }

}
