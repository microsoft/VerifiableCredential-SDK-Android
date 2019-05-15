import { HubErrorCode } from '@decentralized-identity/hub-common-js';
import HubSession from '../../src/hub/HubSession';
import HubCommitWriteRequest from '../../src/hub/requests/HubCommitWriteRequest';
import HubWriteResponse from '../../src/hub/responses/HubWriteResponse';
import HubCommitQueryResponse from '../../src/hub/responses/HubCommitQueryResponse';
import HubObjectQueryResponse from '../../src/hub/responses/HubObjectQueryResponse';
import HubError from '../../src/hub/HubError';
import SignedCommit from '../../src/hub/SignedCommit';
import { PrivateKey, KeyStoreMem, IKeyStore, PrivateKeyRsa } from '@decentralized-identity/did-auth-jose';
import MockResolver from './MockResolver';
import { Request, Response } from 'node-fetch';
import MockHub from './MockHub';

let clientPrivateKey: PrivateKey;
const clientDid = 'did:fake:client.id';
const clientKid = `${clientDid}#key-1`;

let hubPrivateKey: PrivateKeyRsa;
const hubDid = 'did:fake:hub.id';
const hubKid = `${hubDid}#key-1`;

let mockResolver: MockResolver;

const signedCommit = new SignedCommit({
  protected: "test",
  payload: "test",
  signature: "test"
});

describe('HubSession', () => {

  beforeEach(async () => {
    mockResolver = new MockResolver();

    clientPrivateKey = await PrivateKeyRsa.generatePrivateKey(clientKid);
    hubPrivateKey = await PrivateKeyRsa.generatePrivateKey(hubKid);

    mockResolver.setKey(hubDid, hubPrivateKey.getPublicKey());
    mockResolver.setKey(clientDid, clientPrivateKey.getPublicKey());
  });

  describe('send()', () => {

    let session: HubSession;
    let mockHub: MockHub;

    beforeEach(async () => {

      mockHub = new MockHub({
        hubDid,
        hubPrivateKey,
        resolver: mockResolver
      });
      
      const kid = 'testkey';
      const keyStore: IKeyStore = new KeyStoreMem();
      keyStore.save(kid, clientPrivateKey);

      session = new HubSession({
        clientDid: 'did:fake:client.id',
        clientPrivateKeyReference: kid,
        targetDid: 'did:fake:target.id',
        hubDid,
        hubEndpoint: 'https://example.com',
        resolver: mockResolver,
        keyStore: keyStore
      });

      // Redirect fetch() calls to mockHub
      spyOn<any>(session, 'callFetch').and.callFake((url: string | Request, init?: RequestInit) => {
        return mockHub.handleFetch(url, init);
      });

    });

    it('should send a valid request', async () => {
      const request = new HubCommitWriteRequest(signedCommit);

      const mockWriteResponse = JSON.stringify({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'WriteResponse',
        'revisions': ['abc', '123'],
      });

      // Set Hub behavior
      mockHub.setHandler(async (callDetails) => {

        // Expect an auth token request
        if (callDetails.isAuthTokenRequest) {
          return callDetails.authTokenResponse;
        }

        // Then return real response
        return mockWriteResponse;

      });
      
      const response = await session.send(request);
      expect(response.getRevisions()).toEqual(['abc', '123']);
    });

    it('should refresh an invalid access token', async () => {
      const request = new HubCommitWriteRequest(signedCommit);

      session['currentAccessToken'] = 'invalid-access-token';

      const mockWriteResponse = JSON.stringify({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'WriteResponse',
        'revisions': ['abc', '123'],
      });

      let firstRequest = true;

      mockHub.setPreAuthHandler(async (_) => {
        if (firstRequest) {
          firstRequest = false;
          return new Response(JSON.stringify({
            '@type': 'ErrorResponse',
            error_code: HubErrorCode.AuthenticationFailed
          }), { status: 500 });
        }
        return;
      });

      // Set Hub behavior
      mockHub.setHandler(async (callDetails) => {

        // First (invalid) token request handled with preAuthHandler

        // Expect an auth token request
        if (callDetails.isAuthTokenRequest) {
          return callDetails.authTokenResponse;
        }

        // Then return real response
        return mockWriteResponse;

      });
      
      const response = await session.send(request);
      expect(response.getRevisions()).toEqual(['abc', '123']);
    });

    it('should pass through a hub error', async () => {
      const request = new HubCommitWriteRequest(signedCommit);

      const errorResponse = {
        '@type': 'ErrorResponse',
        error_code: HubErrorCode.TooManyRequests
      };

      // Set Hub behavior
      mockHub.setHandler(async (callDetails) => {

        // Expect an auth token request
        if (callDetails.isAuthTokenRequest) {
          return callDetails.authTokenResponse;
        }

        // Then return real response
        return new Response(JSON.stringify(errorResponse), { status: 500 });

      });

      try {
        console.log('test');
        const response = await session.send(request);
        fail('Not expected to reach this point.');
      } catch (e) {
        expect(HubError.is(e)).toBeTruthy();
        expect((<HubError> e).getErrorCode()).toEqual(HubErrorCode.TooManyRequests);
      }
    });

    it('should handle invalid json', async () => {
      const request = new HubCommitWriteRequest(signedCommit);

      // Set Hub behavior
      mockHub.setHandler(async (callDetails) => {
        // Expect an auth token request
        if (callDetails.isAuthTokenRequest) {
          return callDetails.authTokenResponse;
        }

        // Then return real response
        return "not-json";
      });

      try {
        const response = await session.send(request);
        fail('Not expected to reach this point.');
      } catch (e) {
        expect(HubError.is(e)).toBeTruthy();
        expect((<HubError> e).getErrorCode()).toEqual(HubErrorCode.ServerError);
      }
    });

  });

  describe('mapResponseToObject()', () => {
    it('should correctly map responses', () => {
      const method = HubSession['mapResponseToObject'];

      const mapping: {[key: string]: any} = {
        CommitQueryResponse: HubCommitQueryResponse,
        ObjectQueryResponse: HubObjectQueryResponse,
        WriteResponse: HubWriteResponse,
      };

      for (const key in mapping) {
        const response = method({
          '@context': 'https://schema.identity.foundation/0.1',
          '@type': key,
        });
        expect(response instanceof mapping[key]).toBeTruthy();
      }
    });

    it('should map and throw an error response', () => {
      try {
        HubSession['mapResponseToObject'](<any> {
          '@context': 'https://schema.identity.foundation/0.1',
          '@type': 'ErrorResponse',
          error_code: HubErrorCode.AuthenticationFailed,
        });
        fail('Not expected to reach this point.');
      } catch (e) {
        expect(HubError.is(e)).toBeTruthy();
        expect((<HubError> e).getErrorCode()).toEqual(HubErrorCode.AuthenticationFailed);
      }
    });

    it('should throw an error for an unknown response type', () => {
      try {
        HubSession['mapResponseToObject']({
          '@context': 'https://schema.identity.foundation/0.1',
          '@type': 'UnsupportedResponse',
        });
        fail('Not expected to reach this point.');
      } catch (e) {
        expect(HubError.is(e)).toBeTruthy();
        expect((<HubError> e).getErrorCode()).toEqual(HubErrorCode.NotImplemented);
      }
    });
  });

});
