/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import ICredential from 'src/credentials/ICredential';
import Identifier from 'src/Identifier';
// import UserAgentError from '../UserAgentError';
import { Claim } from 'src/types';

/**
 * Implementation of an OpenID Connect
 * self-issued id token.
 * @implements ICredential
 */
export default class SelfIssuedCredential implements ICredential {

  /**
   * Array to hold claims to be included in the credential
   */
  private claims: Claim[] = [];

  /**
   * The identifier the credential was
   * issued to.
   * @inheritdoc
   */
  public readonly issuedBy: Identifier;

  /**
   * The identifier of the issuer of
   * the credential.
   * @inheritdoc
   */
  public readonly issuedTo: Identifier;

  /**
   * The date the credential was issued.
   * @inheritdoc
   */
  public readonly issuedAt: Date;

  /**
   * The date and time that the
   * credential expires at.
   * @inheritdoc
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

    // Need to get the public key from the identifier
    // and sub_jwk
    // issuer
    // .getPublicKey()
    // .then((publicKey: any) => {
    //   this.addClaim({ name: 'sub_jwk', value: publicKey });
    // })
    // .catch(error => {
    //   throw new UserAgentError(error);
    // });
  }

  /**
   * Adds the specified claim to the credential.
   * @param claim claim to add to credential.
   */
  public addClaim (claim: Claim) {
    // Add the claim to the credential
    this.claims.push(claim);
  }

  /**
   * Used to control the the properties that are
   * output by JSON.stringify.
   */
  // public toJSON (): any {
    // The JSON representation of the credential
    // MUST conform to the OpenID Connect
    // Self-Issued specification id token

    // Use a JWT lib for creating the actual
    // JWT for serializing.

    // Need to generate thumbprint for the
}
