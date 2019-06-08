/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import MessageSigningStrategy from "./MessageSigningStrategy";
 import EncryptionStrategy from "./EncryptionStrategy";

 export enum ProtectionStrategyScope {
  /**
   * The protection strategy is applied to the type
   */
  type,

  /**
   * The protection strategy is applied to the object
   */
  object
 }

/**
 * Class used to model protection strategy
 */
export default class ProtectionStrategy {
  /**
   * Gets or sets the scope on which the protection strategy is used.
   */
  public scope: ProtectionStrategyScope = ProtectionStrategyScope.type;
  
  /**
   * Gets or sets the message signing strategy.
   */
  public messageSigningStrategies: MessageSigningStrategy = new MessageSigningStrategy();

  /**
   * Gets or sets the message signing strategy.
   */
   public encryptionStrategy: EncryptionStrategy = new EncryptionStrategy();
  }
