/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import JoseConstants from "../protocols/jose/JoseConstants";
import { IPayloadProtectionStrategy } from "./IPayloadProtectionStrategy";
import IPayloadProtectionOptions from "../protocols/IPayloadProtectionOptions";

/**
 * Class used to model encryption strategies
 */
export default class PayloadEncryptionStrategy implements IPayloadProtectionStrategy {
  /**
   * Get or set a value to enable or disable encryption
   */
  public enabled: boolean = true;
    
  /**
   * Get or set the serialization format.
   */
  public serializationFormat: string = JoseConstants.serializationJweFlatJson;
  
  /**
   * Options used for the protection mechanisms
   */
  public protectionOptions: IPayloadProtectionOptions = <IPayloadProtectionOptions>{};

  /**
   * Get or set the key encryption algorithm as JWA.
   */
   public keyEncrypterAlgorithm: string = JoseConstants.RsaOaep256;
   
  /**
   * Get or set the key encryption algorithm as JWA.
   */
   public symmetricEncrypterAlgorithm: string = JoseConstants.AesGcm128;
  
}
