/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HostServiceEndpoint from "../../src/serviceEndpoints/HostServiceEndpoint";

const context = 'test.schema.identity.foundation/hub';
const location = 'https://test-hub.com';

describe('HostServiceEndpoint', () => { 
  const mockServiceEndpoint: any = {
    locations: [
      location
    ],
    '@context': context,
    '@type': 'HostServiceEndpoint'
  };
  
  let mockServiceEndpointStr: string;

  let endpoint: HostServiceEndpoint;

  beforeAll(() => {
    endpoint = new HostServiceEndpoint(context, [location]);
    mockServiceEndpointStr = JSON.stringify(mockServiceEndpoint);
  });

  it('should create a new HostServiceEndpoint object', () => {
    expect(endpoint).toBeDefined();
    expect(endpoint instanceof HostServiceEndpoint).toBeTruthy();
    expect(endpoint.type).toEqual('HostServiceEndpoint');
    expect(endpoint.context).toEqual(context);
    expect(endpoint.locations[0]).toEqual(location);
  });

  it('should serialize HostServiceEndpoint', () => {
    const serviceEndpointStr = JSON.stringify(endpoint);
    expect(serviceEndpointStr).toEqual(mockServiceEndpointStr);
  });

  it('should deserialize HostServiceEndpoint', () => {
    const serviceEndpoint = <HostServiceEndpoint> HostServiceEndpoint.fromJSON(JSON.parse(mockServiceEndpointStr));
    console.log(serviceEndpoint);
    expect(serviceEndpoint).toEqual(endpoint);
  });
});
