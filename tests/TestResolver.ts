/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import IResolver from 'src/resolvers/IResolver';
import Identifier from 'src/Identifier';
import IdentifierDocument from 'src/IdentifierDocument';

/**
 * Implementation of a resolver for testing.
 * @class
 * @implmenets Resolver
 */
export default class TestResolver implements IResolver {
  private identifier: any;
  private identifierDocument: any;

  /**
   * Prepares the resolver for the test run.
   * @param identifier to use for the test.
   * @param identifierDocument to use for the test.
   */
  public prepareTest (identifier: Identifier, identifierDocument: IdentifierDocument) {
    this.identifier = identifier;
    this.identifierDocument = identifierDocument;
  }

  /**
   * Sends a fetch request to the resolver URL including the
   * specified identifier.
   */
  public async resolve (identifier: Identifier): Promise<IdentifierDocument> {
    if (this.identifier.id === identifier.id) {
      return this.identifierDocument;
    }

    throw new Error('Not found');
  }
}
