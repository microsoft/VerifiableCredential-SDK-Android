/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import PayloadSigningStrategy from "./PayloadSigningStrategy";
 import PayloadEncryptionStrategy from "./PayloadEncryptionStrategy";

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
  public payloadSigningStrategy: PayloadSigningStrategy = new PayloadSigningStrategy();

  /**
   * Gets or sets the message signing strategy.
   */
   public PayloadEncryptionStrategy: PayloadEncryptionStrategy = new PayloadEncryptionStrategy();
  }
