/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { UriDescription } from '../types';
import { ClaimObject } from './models/ClaimObject';
import { ClaimClass } from './models/ClaimClass';
import { ClaimDetails } from '..';
import JwtClaimDetails from '../claimDetails/JwtClaimDetails';

/**
 * Interface defining methods and properties for a Claim object.
 * The properties such as issuer, logo, name, and descriptions are what are meant to be rendered on the claim UI.
 * TODO: figure out what properties exactly we want on a claim.
 */
export default class Claim {

  /**
   * The claim details present in the claim.
   * This includes the verifiable part of the claim.
   * TODO: claimDetails needs to be abstracted to ClaimDetails interface somehow.
   */
  public claimDetails: ClaimDetails | undefined;

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
    this.issuer = claimClass.issuerName;
    this.logo = claimClass.claimLogo.sourceUri;
    this.name = claimClass.claimName;
    this.descriptions = claimClass.claimDescriptions;

    const claimDetailsObject = claimObject.claimDetails;
    if (claimDetailsObject.type === 'jws') {
      this.claimDetails = JwtClaimDetails.create(claimDetailsObject.data);
    }
  }

  /**
   * Creates a new instance of the Claim class.
   * TODO: figure out what properties we want to have on the claim.
   */
  // public static create () {}

  /**
   * Get the claimDetails
   */
  public getClaimDetails () {
    return this.claimDetails;
  }

  /**
   * Get the UI properties in order to render claim correctly
   * TODO: figure out exactly what properties we want to render on the claim.
   */
  public getUIProperties () {
    const uiproperties = {
      issuerName: this.issuer,
      claimName: this.name,
      descriptions: this.descriptions
    };
    return uiproperties;
  }

}
