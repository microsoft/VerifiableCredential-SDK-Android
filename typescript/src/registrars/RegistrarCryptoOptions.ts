/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import ProtectionStrategy from '../crypto/strategies/ProtectionStrategy';
import { IPayloadProtection } from '../crypto/protocols/IPayloadProtection';
import JoseProtocol from '../crypto/protocols/jose/JoseProtocol';
import PayloadSigningStrategy from '../crypto/strategies/PayloadSigningStrategy';
import JoseConstants from '../crypto/protocols/jose/JoseConstants';
import IPayloadProtectionOptions from '../crypto/protocols/IPayloadProtectionOptions';
import { TSMap } from 'typescript-map';
import CryptoFactory from '../crypto/plugin/CryptoFactory';

/**
 * Class used to model registrar crypto options
 */
export default class RegistrarCryptoOptions {
  private cryptoFactory: CryptoFactory;
  private payloadProtection: IPayloadProtection;

  /**
   * Constructs a new instance of the @class RegistrarCryptoOptions
   * @param cryptoFactory used for crypto operations.
   * @param payloadProtection used for the specified protocol.
   */
  constructor ( cryptoFactory: CryptoFactory, payloadProtection: IPayloadProtection) {
    this.cryptoFactory = cryptoFactory;
    this.payloadProtection = payloadProtection;
  }

  /**
   * Get or set the authentication algorithm.
   * Conform to the JWA standard
   */
  public signingAlgorithm: string = 'ES256K';

  /**
   * Get or set the authentication algorithm.
   * Conform to the JWA standard
   */
  public get protectionStrategy(): ProtectionStrategy {
    const strategy = <ProtectionStrategy> {
      payloadSigningStrategy: new PayloadSigningStrategy()
    }
    strategy.payloadSigningStrategy.protectionOptions = <IPayloadProtectionOptions> {
      cryptoFactory: this.cryptoFactory,
      options: new TSMap<string, any>([
        [JoseConstants.optionHeader, new TSMap<string, any>([
          ['alg', ''],
          ['kid', ''],
          ['operation', 'create'],
          ['proofOfWork', '{}']
        ]) ],
      ]),
      payloadProtection: this.payloadProtection
    };
    return strategy;
  }
  
}
