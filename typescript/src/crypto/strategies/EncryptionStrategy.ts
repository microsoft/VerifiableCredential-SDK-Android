/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import JoseConstants from "../protocols/jose/JoseConstants";

/**
 * Class used to model encryption strategies
 */
export default class EncryptionStrategy {
  /**
   * Get or set a value to enable or disable encryption
   */
  public enabled: boolean = true;

  /**
   * Get or set the key encryption algorithm as JWA.
   */
   public keyEncrypterJoseAlgorithm: string = JoseConstants.RsaOaep256;
   
  /**
   * Get or set the key encryption algorithm as JWA.
   */
   public symmetricEncrypterJoseAlgorithm: string = JoseConstants.AesGcm128;
   
}
