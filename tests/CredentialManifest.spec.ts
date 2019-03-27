/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CredentialManifest from '../src/CredentialManifest';
import { DataInput, CredentialManifestIssuerOptions } from '../src/types';

describe('ClaimManifest', () => {

  const testCredential = 'TestCredential';

  const testLanguage = ['en'];

  const testKeeper = 'did:test:example123';

  const testVersion = 'v1';

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
    'preconditions': testPreconditions,
    'inputs': testInputs,
    'issuer_options': testIssuerOptions
  };

  let credentialManifest: any;

  beforeEach(() => {

    credentialManifest = CredentialManifest.create(testCredential,
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

  it('should get Input Style Properties', () => {
    spyOn(credentialManifest, 'getInputStyleProperties').and.callThrough();
    const styleProperties = credentialManifest.getInputStyleProperties();
    expect(credentialManifest.getInputStyleProperties).toHaveBeenCalled();
    expect(styleProperties).toEqual(testStyles);
  });

  it('should get Input Labels', () => {
    spyOn(credentialManifest, 'getInputLabels').and.callThrough();
    const styleProperties = credentialManifest.getInputLabels();
    expect(credentialManifest.getInputLabels).toHaveBeenCalled();
    expect(styleProperties).toEqual(testLabels);
  });

  it('should get Presentation Options', () => {
    spyOn(credentialManifest, 'getPresentationOptions').and.callThrough();
    const styleProperties = credentialManifest.getPresentationOptions();
    expect(credentialManifest.getPresentationOptions).toHaveBeenCalled();
    expect(styleProperties).toEqual(testPresentation);
  });
});
