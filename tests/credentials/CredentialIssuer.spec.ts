/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CredentialIssuer from '../../src/credentials/CredentialIssuer';
import Identifier from '../../src/Identifier';
import UserAgentError from '../../src/UserAgentError';
import CredentialManifest from '../../src/credentials/CredentialManifest';
import TestDataHandler from './TestDataHandler';

let fetchMock: any;

describe('CredentialIssuer', () => {

  let credentialManifest: CredentialManifest;
  let issuerIdentifier: Identifier; 
  let consumer: Identifier;
  let inputCredential: any;
  let testVerifiedCredential: any;

  beforeAll(() => {
    fetchMock = require('fetch-mock');

    credentialManifest = new CredentialManifest({
      credential: 'example',
      endpoint: 'https://enterpriseagent.org/verifiedcredential.json'
    });

    issuerIdentifier = new Identifier('did:test:example.id');
    consumer = new Identifier('did:test:consumer.id');

    
    inputCredential = {
      issuedBy: consumer,
      issuedTo: issuerIdentifier,
      issuedAt: new Date()
    };

    testVerifiedCredential = {
      issuedBy: issuerIdentifier,
      issuedTo: consumer,
      issuedAt: new Date()
    };
  });

  afterEach(() => {
    fetchMock.restore();
  });

  describe('create', () => {
    it('should create a new CredentialIssuer from Credential Manifest Endpoint', async () => {
      fetchMock.get(
        'https://enterpriseagent.org/credentialManifest.json',
        new Promise(resolve => resolve(credentialManifest))
      );

      const issuer: CredentialIssuer = await CredentialIssuer.create(issuerIdentifier,
         'https://enterpriseagent.org/credentialManifest.json');
      expect(issuer).toBeDefined();
      expect(issuer.manifest).toEqual(credentialManifest);
    });

    it('should create a new CredentialIssuer from Credential Manifest Object', async () => {

      const issuer: CredentialIssuer = await CredentialIssuer.create(issuerIdentifier, credentialManifest);
      expect(issuer).toBeDefined();
      expect(issuer.manifest).toEqual(credentialManifest);
    });

    it('should throw an error when fetch returns 404', async () => {
      fetchMock.get('https://enterpriseagent.org/credentialManifest.json', 404);

      try {
        const issuer: CredentialIssuer = await CredentialIssuer.create(issuerIdentifier,
          'https://enterpriseagent.org/credentialManifest.json');
        console.log(issuer);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Failed to request a credential manifest from the issuer 'did:test:example.id.'`);
      }
    });

    it('should throw an error when server returns 500', async () => {
      fetchMock.get('https://enterpriseagent.org/credentialManifest.json', 500);

      try {
        const issuer: CredentialIssuer = await CredentialIssuer.create(issuerIdentifier,
          'https://enterpriseagent.org/credentialManifest.json');
        console.log(issuer);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`'https://enterpriseagent.org/credentialManifest.json' returned an error with 'Internal Server Error'`);
      }
    });
  });

  describe('requestCredential', () => {

    afterEach(() => {
      fetchMock.restore();
    });

    it('should get a verified credential back', async () => {
      const options = { method: 'POST' };
      fetchMock.mock(
        'https://enterpriseagent.org/verifiedcredential.json',
        testVerifiedCredential,
        options
      );

      const issuer = new CredentialIssuer(issuerIdentifier, credentialManifest);
      const verifiedCredential = await issuer.requestCredential(inputCredential);
      expect(verifiedCredential).toBeDefined();
      expect(verifiedCredential.issuedBy.id).toEqual('did:test:example.id');
      expect(verifiedCredential.issuedTo.id).toEqual('did:test:consumer.id');
    });

    it('should throw an error when fetch returns 404', async () => {
      const response = { status: 404, body: testVerifiedCredential };
      const options = { method: 'POST' };
      fetchMock.mock(
        'https://enterpriseagent.org/verifiedcredential.json',
        response,
        options
      );

      try {
        const issuer = new CredentialIssuer(issuerIdentifier, credentialManifest);
        const verifiedCredential = await issuer.requestCredential(inputCredential);
        console.log(verifiedCredential);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Failed to request a credential from the issuer 'did:test:example.id.'`);
      }
    });

    it('should throw a timeout error', async () => {
      const delay = new Promise((_res, _rej) => setTimeout(_res, 40000));
      // const response = { status: 404, body: VERIFIEDCREDENTIAL };
      // const options = { method: 'POST' };
      fetchMock.mock(
        'https://enterpriseagent.org/verifiedcredential.json',
        delay.then((_response) => 404)
      );

      try {
        const issuer = new CredentialIssuer(issuerIdentifier, credentialManifest);
        const verifiedCredential = await issuer.requestCredential(inputCredential);
        console.log(verifiedCredential);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Requesting a credential from '${credentialManifest.endpoint}' timed out`);
      }
    }, 50000);
  });

  describe('handleCredentialRequest', () => {
    let issuer: CredentialIssuer;
    let testDataHandler: TestDataHandler;

    beforeEach(async () => {
      issuer = await CredentialIssuer.create(issuerIdentifier, credentialManifest);
      testDataHandler = new TestDataHandler();
    });

    it('should throw an error because credential does not match credential manifest', async () => {
      spyOn(issuer, 'validateCredential').and.returnValue(false);
      try {
        await issuer.handleCredentialRequest(inputCredential, testDataHandler);
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(
          `Credential issued by '${inputCredential.issuedBy.id}' does not match credential manifest '${credentialManifest.credential}'`
          );
      }
    });

    it('should handle the Credential Request and return new Credential', async () => {
      const credential = await issuer.handleCredentialRequest(inputCredential, testDataHandler);
      expect(credential.issuedTo).toEqual(inputCredential.issuedBy);
      expect(credential.issuedBy).toEqual(inputCredential.issuedTo);
      expect(credential.issuedAt).toBeDefined();
    });

  });
});
