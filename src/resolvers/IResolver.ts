/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from 'src/Identifier';
import IdentifierDocument from 'src/IdentifierDocument';

/**
 * Interface defining methods and properties to
 * be implemented by specific resolver methods.
 */
export default interface IResolver {
  /**
   * Returns the identifier document for the specified
   * identifier.
   * @param identifier for which to return the identifier document.
   */
  resolve (identifier: Identifier): Promise<IdentifierDocument>;
}
