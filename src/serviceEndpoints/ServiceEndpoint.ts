/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

/**
 * abstract class to represent a service endpoint.
 * based on: https://github.com/decentralized-identity/identity-hub/blob/master/explainer.md.
 */
export default abstract class ServiceEndpoint {
  /**
   * The context of the service reference.
   */
  public context: string;

  /**
   * The type of the service reference.
   */
  public type: string;

  constructor(context: string, type: string) {
    this.context = context;
    this.type = type;
  }
}