/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import JoseConstants from "../protocols/jose/JoseConstants";

/**
 * Class used to model the message signing strategy
 */
export default class MessageSigningStrategy {
  /**
   * Get or set a value to enable or disable message signing
   */
  public enabled: boolean = true;

  /**
   * Get or set the key message signing algorithm as JWA.
   */
   public messageSignerJoseAlgorithm: string = JoseConstants.Es256K;
   
  /**
   * Get or set the key encryption algorithm as JWA.
   */
   public digestJoseAlgorithm: string = JoseConstants.Sha256;
   
}
