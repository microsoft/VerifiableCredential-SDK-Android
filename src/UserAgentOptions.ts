
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Resolver from './resolvers/Resolver';
import Registrar from './registrars/Registrar';
import KeyStore from './keystores/KeyStore';

/**
 * Interface defining options for the
 * User Agent, such as resolver and register.
 */
export default class UserAgentOptions {
  /**
   * Instance of Resolver than can be used
   * to resolve identifiers.
   */
  resolver?: Resolver;

  /**
   * Instance of Registar than can be used
   * to register identifiers.
   */
  registrar?: Registrar;

  /**
   * Instance of KeyStore than can be used
   * to get and save keys.
   */
  keyStore?: KeyStore;

  /**
   * The timeout when making requests to
   * external services.
   */
  timeoutInSeconds: number = 30;
}
