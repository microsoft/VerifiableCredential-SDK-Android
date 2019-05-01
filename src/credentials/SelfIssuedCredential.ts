/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from './ICredential';
import Identifier from '../Identifier';
import { Claim } from '../types';

/**
 * Implementation of an OpenID Connect
 * self-issued id token.
 * @implements ICredential
 */
export default class SelfIssuedCredential implements ICredential {

  /**
   * Array to hold claims to be included in the credential
   */
  private claims: Array<Claim> = [];

  /**
   * The identifier of the issuer of
   * the credential.
   */
  public readonly issuedBy: Identifier;

  /**
   * The identifier the credential was
   * issued to.
   */
  public readonly issuedTo: Identifier;

  /**
   * The date the credential was issued.
   */
  public readonly issuedAt: Date;

  /**
   * The date and time that the
   * credential expires at.
   */
  public readonly expiresAt?: Date;

  /**
   * Constructs a new instance of a self-issued
   * credential for the specified identifier.
   * @param issuer of the credential.
   * @param recipient either a string or identifier identifying the
   * intended recipient of the credential.
   */
  constructor (issuer: Identifier, recipient: Identifier) {

    this.issuedBy = issuer;
    this.issuedTo = recipient;
    this.issuedAt = new Date(Date.now());

    // Add the identifier as the did claim
    this.addClaim({ name: 'did', value: issuer.id });
  }

  /**
   * Adds the specified claim to the credential.
   * @param claim claim to add to credential.
   */
  public addClaim (claim: Claim) {
    // Add the claim to the credential
    this.claims.push(claim);
  }
}
