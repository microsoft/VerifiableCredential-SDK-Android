/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import CryptoFactory from './crypto/plugin/CryptoFactory';
import SubtleCryptoBrowserOperations from './crypto/plugin/SubtleCryptoBrowserOperations';
import KeyStoreInMemory from './crypto/keyStore/KeyStoreInMemory';
import { IPayloadProtection } from './crypto/protocols/IPayloadProtection';
import JoseProtocol from './crypto/protocols/jose/JoseProtocol';

/**
 * Class used to model crypto options
 */
export default class CryptoOptions {
  /**
   * Get or set the crypto api to be used. Initialize the default crypto plugin.
   */
  public cryptoFactory: CryptoFactory = new CryptoFactory(new KeyStoreInMemory(), new SubtleCryptoBrowserOperations());

  /**
   * Get or set the payload protection protocol.
   */
  public payloadProtection: IPayloadProtection = new JoseProtocol();

  /**
   * Get or set the signing algorithm.
   */
  public signingAlgorithm: string = 'ES256K';

  /**
   * Get or set the encryption algorithm.
   */
  public encryptionAlgorithm: string = 'RSA-OAEP';

   /**
   * Key reference to private key to be used to sign commits and create HubSession
   */
  public signingKeyReference: string | undefined;

   /**
   * Key reference to private key to be used to decrypt commits and create HubSession
   */
  public encryptionKeyReference: string | undefined;
}
