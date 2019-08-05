/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import UserServiceEndpoint from '../../src/serviceEndpoints/UserServiceEndpoint';

const context = 'test.schema.identity.foundation/hub';
const instance = 'did:test:hub.id';

describe('UserServiceEndpoint', () => { 
  const mockServiceEndpoint: any = {
    instances: [
      'did:test:hub.id'
    ],
    '@context': 'test.schema.identity.foundation/hub',
    '@type': 'UserServiceEndpoint'
  };

  let mockServiceEndpointStr: string;

  let endpoint: UserServiceEndpoint;

  beforeAll(() => {
    endpoint = new UserServiceEndpoint(context, [instance]);
    mockServiceEndpointStr = JSON.stringify(mockServiceEndpoint);
  });

  it('should create a new UserServiceEndpoint object', () => {
    expect(endpoint).toBeDefined();
    expect(endpoint instanceof UserServiceEndpoint).toBeTruthy();
    expect(endpoint.type).toEqual('UserServiceEndpoint');
    expect(endpoint.context).toEqual(context);
    expect(endpoint.instances[0]).toEqual(instance);
  });

  it('should serialize UserServiceEndpoint', () => {
    const serviceEndpointStr = JSON.stringify(endpoint);
    expect(serviceEndpointStr).toEqual(mockServiceEndpointStr);
  });

  it('should deserialize UserServiceEndpoint', () => {
    const serviceEndpoint = <UserServiceEndpoint> UserServiceEndpoint.fromJSON(JSON.parse(mockServiceEndpointStr));
    console.log(serviceEndpoint);
    expect(serviceEndpoint).toEqual(endpoint);
  });
});
