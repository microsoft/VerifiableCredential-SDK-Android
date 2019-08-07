
/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IResolver from './resolvers/IResolver';
import IRegistrar from './registrars/IRegistrar';
import CryptoOptions from './CryptoOptions';
import IKeyStore from './crypto/keyStore/IKeyStore';
import CryptoFactory from './crypto/plugin/CryptoFactory';

/**
 * Interface defining options for the
 * User Agent, such as resolver and register.
 */
export default class UserAgentOptions {
  /**
   * Instance of Resolver than can be used
   * to resolve identifiers.
   */
   public resolver?: IResolver;

  /**
   * Instance of Registrar than can be used
   * to register identifiers.
   */
  public registrar?: IRegistrar;

  /**
   * The timeout when making requests to
   * external services.
   */
   public timeoutInSeconds: number = 30;

  /**
   * The locale to be used by the
   * user agent.
   */
   public locale: string = 'en';

  /**
   * The lifetime of any self-issued credentials.
   * Used to determine the expiry time of the
   * credential.
   */
   public selfIssuedCredentialLifetimeInSeconds: number = 300; // 5 mins

  /**
   * Crypto Options
   * contains algorithm and other data about crypto
   */
   public cryptoOptions: CryptoOptions = new CryptoOptions();

  /**
   * Prefix for the generated did.
   */
   public didPrefix: string = 'did:ion';

  /**
   * Get the key store
   */
   public get keyStore (): IKeyStore {
    return this.cryptoOptions!.cryptoFactory.keyStore;
  }

  /**
   * Set the key store
   */
   public set keyStore (keyStore: IKeyStore) {
    this.cryptoOptions!.cryptoFactory.keyStore = keyStore;
  }

  /**
   * Get the crypto operations
   */
   public get cryptoFactory (): CryptoFactory {
    return this.cryptoOptions!.cryptoFactory;
  }

  /**
   * Set the key store
   */
   public set cryptoFactory (cryptoFactory: CryptoFactory) {
    this.cryptoOptions!.cryptoFactory = cryptoFactory;
  }
}
