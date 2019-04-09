/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import CredentialIssuer from '../../src/credentials/CredentialIssuer';
import Identifier from '../../src/Identifier';
import UserAgentError from '../../src/UserAgentError';
import { CredentialManifest } from '../../src';
const fetchMock = require('fetch-mock');

describe('CredentialIssuer', () => {

  afterEach(() => {
    fetchMock.restore();
  });

  const CREDENTIALMANIFEST = new CredentialManifest({
    credentailManifest: 'example',
    endpoint: 'https://enterpriseagent.org/verifiedcredential.json'
  });

  const issuerIdentifier = new Identifier('did:test:example.id');
  const consumer = new Identifier('did:test:consumer.id');

  const inputCredential = {
    issuedBy: consumer,
    issuedTo: issuerIdentifier,
    issuedAt: new Date()
  };

  const VERIFIEDCREDENTIAL = {
    issuedBy: issuerIdentifier,
    issuedTo: consumer,
    issuedAt: new Date()
  };

  describe('create', () => {
    it('should create a new CredentialIssuer from Credential Manifest Endpoint', async done => {
      fetchMock.get(
        'https://enterpriseagent.org/credentialManifest.json',
        new Promise(resolve => resolve(CREDENTIALMANIFEST))
      );

      const issuer: CredentialIssuer = await CredentialIssuer.create(issuerIdentifier,
         'https://enterpriseagent.org/credentialManifest.json');
      expect(issuer).toBeDefined();
      expect(issuer.manifest).toEqual(CREDENTIALMANIFEST);
      done();
    });

    it('should create a new CredentialIssuer from Credential Manifest Object', async done => {

      const issuer: CredentialIssuer = await CredentialIssuer.create(issuerIdentifier, CREDENTIALMANIFEST);
      expect(issuer).toBeDefined();
      expect(issuer.manifest).toEqual(CREDENTIALMANIFEST);
      done();
    });

    it('should throw an error when fetch returns 404', async done => {
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
      done();
    });

    it('should throw an error when server returns 500', async done => {
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
      done();
    });
  });

  describe('requestCredential', () => {

    afterEach(() => {
      fetchMock.restore();
    });

    it('should get a verified credential back', async done => {
      const response = { status: 200, body: VERIFIEDCREDENTIAL, statusText: 'OK' };
      const options = { method: 'POST' };
      fetchMock.mock(
        'https://enterpriseagent.org/verifiedcredential.json',
        response,
        options
      );

      const issuer = new CredentialIssuer(issuerIdentifier, CREDENTIALMANIFEST);
      const verifiedCredential = await issuer.requestCredential(inputCredential);
      expect(verifiedCredential).toBeDefined();
      expect(verifiedCredential.issuedBy.id).toEqual('did:test:example.id');
      expect(verifiedCredential.issuedTo.id).toEqual('did:test:consumer.id');
      done();
    });

    it('should throw an error when fetch returns 404', async done => {
      const response = { status: 404, body: VERIFIEDCREDENTIAL };
      const options = { method: 'POST' };
      fetchMock.mock(
        'https://enterpriseagent.org/verifiedcredential.json',
        response,
        options
      );

      try {
        const issuer = new CredentialIssuer(issuerIdentifier, CREDENTIALMANIFEST);
        const verifiedCredential = await issuer.requestCredential(inputCredential);
        console.log(verifiedCredential);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Failed to request a credential from the issuer 'did:test:example.id.'`);
      }
      done();
    });

    it('should throw a timeout error', async done => {
      const delay = new Promise((res, _rej) => setTimeout(res, 40000));
      // const response = { status: 404, body: VERIFIEDCREDENTIAL };
      // const options = { method: 'POST' };
      fetchMock.mock(
        'https://enterpriseagent.org/verifiedcredential.json',
        delay.then((_response) => 404)
      );

      try {
        const issuer = new CredentialIssuer(issuerIdentifier, CREDENTIALMANIFEST);
        const verifiedCredential = await issuer.requestCredential(inputCredential);
        console.log(verifiedCredential);
        fail();
      } catch (error) {
        expect(error).toBeDefined();
        expect(error instanceof UserAgentError).toBeTruthy();
        expect(error.message).toEqual(`Requesting a credential from '${CREDENTIALMANIFEST.endpoint}' timed out`);
      }
      done();
    }, 50000);
  });
});
