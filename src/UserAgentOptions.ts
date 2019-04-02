
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IResolver from './resolvers/IResolver';
import IRegistrar from './registrars/IRegistrar';
import IKeyStore from './keystores/IKeyStore';

/**
 * Interface defining options for the
 * User Agent, such as resolver and register.
 */
export default class UserAgentOptions {
  /**
   * Instance of Resolver than can be used
   * to resolve identifiers.
   */
  resolver?: IResolver;

  /**
   * Instance of Registrar than can be used
   * to register identifiers.
   */
  registrar?: IRegistrar;

  /**
   * Instance of KeyStore than can be used
   * to get and save keys.
   */
  keyStore?: IKeyStore;

  /**
   * The timeout when making requests to
   * external services.
   */
  timeoutInSeconds?: number = 30;

  /**
   * The locale to be used by the
   * user agent.
   */
  locale?: string = 'en';

  /**
   * The lifetime of any self-issued credentials.
   * Used to determine the expiry time of the
   * credential.
   */
  selfIssuedCredentialLifetimeInSeconds?: number = 300; // 5 mins
}
