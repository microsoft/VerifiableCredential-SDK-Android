/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ClaimDetails from '../claimDetails/ClaimDetails';
import { UriDescription } from '../types';

/**
 * Interface defining methods and properties for a Claim object.
 * The properties such as issuer, logo, name, and descriptions are what are meant to be rendered on the claim UI.
 */
export default interface Claim {

  /**
   * The claim details present in the claim.
   * This includes the verifiable part of the claim.
   */
  claimDetails: ClaimDetails;

  /**
   * Issuer Name
   */
  issuer: string;

  /**
   * Issuer Logo
   */
  logo: UriDescription;

  /**
   * Claim Name
   */
  name: string;

  /**
   * Claim Descriptions
   */
  descriptions: any;

}
