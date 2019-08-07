/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import { TSMap } from "typescript-map";

/**
 * Genereric type to model crypto tokens
 */
export interface ICryptoToken extends TSMap<string, any> {
  /**
   * get the format of the crypto token
   */
  tokenFormat(): string;

  /**
   * Serialize a a @interface ICryptoToken 
   */
   serialize(): string;
  
  /**
   * Convert token to json 
   */
   toJson(): any;
  }
