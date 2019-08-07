/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import JoseConstants from "../protocols/jose/JoseConstants";
import { IPayloadProtectionStrategy } from "./IPayloadProtectionStrategy";
import IPayloadProtectionOptions from "../protocols/IPayloadProtectionOptions";

/**
 * Class used to model the message signing strategy
 */
export default class PayloadSigningStrategy implements IPayloadProtectionStrategy{
  /**
   * Get or set a value to enable or disable message signing
   */
  public enabled: boolean = true;
   
  /**
   * Get or set the serialization format.
   */
  public serializationFormat: string = JoseConstants.serializationJwsFlatJson;
  
  /**
   * Options used for the protection mechanisms
   */
  public protectionOptions: IPayloadProtectionOptions = <IPayloadProtectionOptions>{};

  /**
   * Get or set the key message signing algorithm as JWA.
   */
   public payloadSignerAlgorithm: string = JoseConstants.Es256K;
   
  /**
   * Get or set the key encryption algorithm as JWA.
   */
  public digestAlgorithm: string = JoseConstants.Sha256;
   
}
