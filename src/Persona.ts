/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Identifier from './Identifier';

/**
 * Class for creating and managing a persona.
 */
export default class Persona {

  /**
   * The identifier that is owned by this persona
   */
  public identifier: Identifier;

  /**
   * the name of the persona
   */
  public name: string;

  /**
   * Constructs an instance of the Persona class using the
   * provided Identifier or identifier string, and the name of the Persona
   * @param identifier either the string representation of an identifier or an Identifier Object
   * @param name the name of the persona
   */
  constructor (identifier: Identifier | string, name: string) {

    if (typeof identifier === 'string') {
      this.identifier = new Identifier(identifier, { timeoutInSeconds: 30 });
    } else {
      this.identifier = identifier;
    }

    this.name = name;
  }

}
