/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import base64url from 'base64url';
import ProtectedCommit from '../../src/hubSession/ProtectedCommit';
import JoseToken from '../../src/crypto/protocols/jose/JoseToken';
import ProtocolTest from '../crypto/protocols/jose/ProtocolTest';
import JoseConstants from '../../src/crypto/protocols/jose/JoseConstants';
import { ProtectionFormat } from '../../src/crypto/keyStore/ProtectionFormat';
import IPayloadProtectionOptions from '../../src/crypto/protocols/IPayloadProtectionOptions';
import { ICommitProtectedHeaders } from '@decentralized-identity/hub-common-js';

const createHeaders: ICommitProtectedHeaders = {
  interface: 'Collections',
  context: 'schema.org',
  type: 'MusicPlaylist',
  operation: 'create',
  committed_at: '2019-01-01',
  commit_strategy: 'basic',
  sub: 'did:example:sub.id',
  kid: 'did:example:client.id#key-1',
  iss: 'did:example:client.id',
};

describe('ProtectedCommit', () => {

  describe('getProtectedHeaders()', () => {

    it('should return the headers', async () => {
      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, base64url(JSON.stringify({ name: 'test' }))],
        [JoseConstants.tokenProtected, base64url(JSON.stringify(createHeaders))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);

      expect(signedCommit.getProtectedHeaders()).toEqual(createHeaders);
    });

    it('should throw if protected headers are missing', async () => {
      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, base64url(JSON.stringify({ name: 'test' }))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);

      try {
        signedCommit.getProtectedHeaders();
        fail('Should not reach this point.')
      } catch (e) {
        // Expected
      }
    });

  });

  describe('getPayload()', () => {

    it('should return a json payload', async () => {
      const payload = {
        name: 'test'
      };
      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, base64url(JSON.stringify(payload))],
        [JoseConstants.tokenProtected, base64url(JSON.stringify(createHeaders))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);

      expect(signedCommit.getPayload()).toEqual(payload);
    });

    it('should return a non-json payload', async () => {
      const payload = 'test';
      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, base64url(payload)],
        [JoseConstants.tokenProtected, base64url(JSON.stringify(createHeaders))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);

      expect(signedCommit.getPayload()).toEqual(payload);
    });

    it('should throw if a payload is missing', async () => {
      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenProtected, base64url(JSON.stringify(createHeaders))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);

      try {
        signedCommit.getPayload();
        fail('Should not reach this point.')
      } catch (e) {
        // Expected
      }
    });

  });

  describe('getObjectId()', () => {

    it('should return the revision for a create commit', async () => {
      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, base64url(JSON.stringify({ name: 'test '}))],
        [JoseConstants.tokenProtected, base64url(JSON.stringify(createHeaders))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);
      expect(signedCommit.getObjectId()).toEqual(signedCommit.getRevision());
    });

    it('should return the revision for an update commit', async () => {
      
      const updateHeaders = Object.assign({}, createHeaders, {
        operation: 'update',
        object_id: 'abc123'
      });

      const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, base64url(JSON.stringify({ name: 'test' }))],
        [JoseConstants.tokenProtected, base64url(JSON.stringify(updateHeaders))],
        [JoseConstants.tokenSignatures, ['abc']]]);
    
      const signedCommit =  new ProtectedCommit(token);

      expect(signedCommit.getObjectId()).toEqual('abc123');
    });

  });

});
