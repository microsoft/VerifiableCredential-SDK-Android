/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

 /**
  * Enum to define different protection formats
  */
export enum ProtectionFormat {
  /**
   * Format for a flat JSON signature
   */
  JwsFlatJson = 0,

  /**
   * Format for a compact JSON signature
   */
  JwsCompactJson,

  /**
   * Format for a general JSON signature
   */
  JwsGeneralJson,

  /**
   * Format for a flat JSON encryption
   */
  JweFlatJson,

  /**
   * Format for a compact JSON encryption
   */
  JweCompactJson,
  
  /**
   * Format for a general JSON encryption
   */
  JweGeneralJson
}
