/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IPayloadProtectionOptions from '../protocols/IPayloadProtectionOptions';

/**
 * Interface used for protection strategy
 */
export interface IPayloadProtectionStrategy {
  /**
   * Get or set a value to enable or disable message signing
   */
  enabled: boolean;

  /**
   * Get or set the serialization format.
   */
  serializationFormat: string;
  
  /**
   * Options used for the protection mechanisms
   */
  protectionOptions: IPayloadProtectionOptions;
}
