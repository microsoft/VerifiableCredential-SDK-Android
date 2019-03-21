/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CredentialManifest from '../src/CredentialManifest';
import { DataInput } from '../src/types';

 describe('ClaimManifest', () => {

  const testCredential = 'TestCredential';

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

  const testIssuerOptions = {
    style: {
      test: 'test'
    }
  };

  const testManifest = {
    '@context': 'https://identity.foundation/schemas/credentials',
    '@type': 'CredentialManifest',
    credential: testCredential,
    preconditions: testPreconditions,
    inputs: testInputs,
    issuer_options: testIssuerOptions
  };

    it('should create a new CredentialManifest Object', () => {
      spyOn(CredentialManifest, 'create').and.callThrough();
      const manifest = CredentialManifest.create(testCredential, testPreconditions, testInputs, testIssuerOptions);
      expect(CredentialManifest.create).toHaveBeenCalled();
      expect(manifest).toBeDefined();
    });

    it('should form a CredentialManifest correctly', () => {
      const credentialManifest = CredentialManifest.create(testCredential, testPreconditions, testInputs, testIssuerOptions);
      spyOn(credentialManifest, 'toJSON').and.callThrough();
      const manifest = credentialManifest.toJSON();
      expect(credentialManifest.toJSON).toHaveBeenCalled();
      expect(manifest).toEqual(testManifest);
    });

    it('should get Input Properties', () => {
      const credentialManifest = CredentialManifest.create(testCredential, testPreconditions, testInputs, testIssuerOptions);
      spyOn(credentialManifest, 'getInputProperties').and.callThrough();
      const inputProperties = credentialManifest.getInputProperties();
      expect(credentialManifest.getInputProperties).toHaveBeenCalled();
      expect(inputProperties).toEqual(testInputs);
    });

    it('should get Display Properties', () => {
      const credentialManifest = CredentialManifest.create(testCredential, testPreconditions, testInputs, testIssuerOptions);
      spyOn(credentialManifest, 'getDisplayProperties').and.callThrough();
      const displayProperties = credentialManifest.getDisplayProperties();
      expect(credentialManifest.getDisplayProperties).toHaveBeenCalled();
      expect(displayProperties).toEqual(testIssuerOptions.style);
    });
 });
