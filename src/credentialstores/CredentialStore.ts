/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import { HubSession } from '@decentralized-identity/hub-sdk-js';
import ICredentialStore from './ICredentialStore';
import Identifier from '../Identifier';
import IdentifierDocument from '../IdentifierDocument';
import UserAgentError from '../UserAgentError';
import { UserServiceEndpoint } from '../types';
import { UserAgentOptions } from '..';

/**
 * Interface defining methods and properties to
 * be implemented by specific credential stores.
 */
export default class InMemoryCredentialStore implements ICredentialStore {

  private hubSession: HubSession;

  constructor (hubSession: HubSession) {
    this.hubSession = hubSession;
  }

  public async create (identifier: Identifier, publicKeyReference: string, hubInstance?: string) {

    let hubIdentifierDocument: IdentifierDocument;

    if (!identifier.options) {
      throw new UserAgentError(`No User Agent Options defined`);
    }

    const identifierDocument = await identifier.getDocument();
    const serviceReferences = identifierDocument.serviceReferences;

    if (!serviceReferences) {
      throw new UserAgentError(`No service references defined for ${identifier.id}`);
    }

    const filteredServiceReferences = serviceReferences.filter(ref => ref.publicKeyReference === publicKeyReference);

    if (filteredServiceReferences.length !== 1) {
      throw new UserAgentError(`No service references with public key reference: ${publicKeyReference}`);
    }

    // get hub instance identifier document from user identifier document
    if (filteredServiceReferences[0].type === 'UserServiceEndpoint') {

      const userServiceEndpoint = filteredServiceReferences[0].serviceEndpoint as UserServiceEndpoint;
      const hubInstances = userServiceEndpoint.instances;

      if (hubInstances.length > 1 && !hubInstance) {
        throw new UserAgentError(`No Hub Instance specified for ${identifier.id}`);
      }

      if (hubInstances.length === 1) {
        hubIdentifierDocument = await this.getHubEndpointLocation(hubInstances[0], identifier.options);
      } else if (hubInstance) {
        const filteredHubInstances = hubInstances.filter(instance => instance === hubInstance);
        hubIdentifierDocument = await this.getHubEndpointLocation(filteredHubInstances[0], identifier.options);
      } else {
        throw new UserAgentError(`No hub instance reference specified for ${identifier.id}`);
      }
    } else {
      hubIdentifierDocument = identifierDocument;
    }
  }

  private async getHubEndpointLocation (identifierID: string, options: UserAgentOptions) {
    const hubIdentifier = new Identifier(identifierID, options);
    return hubIdentifier.getDocument();
  }

  public async read () {
    throw new Error('not implemented');
  }

  public async write () {
    throw new Error('not implemented');
  }
}
