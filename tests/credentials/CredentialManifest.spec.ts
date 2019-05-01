/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CredentialManifest from '../../src/credentials/CredentialManifest';
import { DataInput } from '../../src/types';
import CredentialManifestIssuerOptions from '../../src/credentials/CredentialManifestIssuerOptions';

describe('ClaimManifest', () => {

  const testCredential = 'TestCredential';

  const testLanguage = ['en'];

  const testKeeper = 'did:test:example123';

  const testVersion = 'v1';

  const testEndpoint = 'http://endpoint.org';

  const testPreconditions = {
    '@type': 'ProofSet',
    'groups': [
      {
        rule: 'all',
        from: ['A']
      }
    ]
  };

  const testInputs: Array<DataInput> = [
    {
      type: 'data',
      group: ['A'],
      field: 'routing_number',
      value: {
        type: 'string'
      }
    }
  ];

  const testStyles = {
    test: 'test'
  };

  const testLabels = {
    routing_number: 'test routing number'
  };

  const testPresentation = {
    issuer_name: 'Test',
    credential_name: 'Test Credential',
    description: 'test description',
    claims: {
      test: {
        label: 'Test Claim',
        type: 'string',
        value: 'routing_number'
      }
    },
    styles: testStyles
  };

  const testIssuerOptions: CredentialManifestIssuerOptions = {
    input: {
      styles: testStyles,
      labels: testLabels
    },
    presentation: testPresentation
  };

  const testManifest = {
    '@context': 'https://identity.foundation/schemas/credentials',
    '@type': 'CredentialManifest',
    'credential': testCredential,
    'endpoint': testEndpoint,
    'preconditions': testPreconditions,
    'inputs': testInputs,
    'issuer_options': testIssuerOptions
  };

  let credentialManifest: any;

  beforeEach(() => {

    credentialManifest = CredentialManifest.create(testCredential,
      testEndpoint,
      testLanguage,
      testKeeper,
      testVersion,
      testPreconditions,
      testInputs,
      testIssuerOptions);

  });

  it('should create a new CredentialManifest Object', () => {
    spyOn(CredentialManifest, 'create').and.callThrough();
    const manifest = CredentialManifest.create(testCredential,
                                               testCredential,
                                               testLanguage,
                                               testKeeper,
                                               testVersion,
                                               testPreconditions,
                                               testInputs,
                                               testIssuerOptions);
    expect(CredentialManifest.create).toHaveBeenCalled();
    expect(manifest).toBeDefined();
  });

  it('should serialize a CredentialManifest correctly', () => {
    spyOn(credentialManifest, 'toJSON').and.callThrough();
    const manifest = credentialManifest.toJSON();
    expect(credentialManifest.toJSON).toHaveBeenCalled();
    expect(manifest).toEqual(testManifest);
  });

  it('should get Keeper DID', () => {
    spyOn(credentialManifest, 'getKeeperDid').and.callThrough();
    const keeper = credentialManifest.getKeeperDid();
    expect(credentialManifest.getKeeperDid).toHaveBeenCalled();
    expect(keeper).toEqual(testKeeper);
  });

  it('should get Input Properties', () => {
    spyOn(credentialManifest, 'getInputProperties').and.callThrough();
    const inputProperties = credentialManifest.getInputProperties();
    expect(credentialManifest.getInputProperties).toHaveBeenCalled();
    expect(inputProperties).toEqual(testInputs);
  });
});
