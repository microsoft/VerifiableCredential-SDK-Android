/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import Registrar from '../src/registrars/Registrar';
import Identifier from '../src/Identifier';
import IdentifierDocument from '../src/IdentifierDocument';

/**
 * Implementation of a registrar for testing.
 */
export default class TestRegistrar implements Registrar {
  private identifier: any;
  private identifierDocument: any;

  /**
   * Prepares the resolver for the test run.
   * @param identifier to use for the test.
   * @param identifierDocument to use for the test.
   */
  public prepareTest(identifier: Identifier, identifierDocument: IdentifierDocument) {
    this.identifier = identifier;
    this.identifierDocument = identifierDocument;
  }

  /**
   * @inheritdoc
   */
  public async register(identifierDocument: IdentifierDocument): Promise<Identifier> {
    if (this.identifierDocument === identifierDocument) {
      return this.identifier;
    }

    throw new Error('Not found');
  }
}
