/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubCommitQueryResponse from '../../../src/hubSession/responses/HubCommitQueryResponse';
import JoseToken from '../../../src/crypto/protocols/jose/JoseToken';
import ProtocolTest from '../../crypto/protocols/jose/ProtocolTest';
import JoseConstants from '../../../src/crypto/protocols/jose/JoseConstants';
import { ProtectionFormat } from '../../../src/crypto/keyStore/ProtectionFormat';
import IPayloadProtectionOptions from '../../../src/crypto/protocols/IPayloadProtectionOptions';

const token = new JoseToken(<IPayloadProtectionOptions> {}, new ProtocolTest(), [
  [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
  [JoseConstants.tokenPayload, 'test'],
  [JoseConstants.tokenProtected, 'test'],
  [JoseConstants.tokenSignatures, ['test']]]);

describe('HubCommitQueryResponse', () => {

  let response: HubCommitQueryResponse;

  beforeAll(() => {
    
    response = new HubCommitQueryResponse({
      '@context': 'https://schema.identity.foundation/0.1',
      '@type': 'CommitQueryResponse',
      commits: [token],
      skip_token: 'abc',
    });
  });

  describe('constructor', () => {
    it('should throw on an invalid response type', async () => {
      try {
        const r = new HubCommitQueryResponse(<any> {
          '@type': 'WrongType',
        });
        fail(`Constructor was expected to throw: '${r}'`);
      } catch (e) {
        // Expected
      }
    });
  });

  describe('getCommits()', () => {
    it('should return the matching commits', async () => {
      const returnedCommits = await response.getCommits();

      expect(returnedCommits.length).toEqual(1);
      expect(returnedCommits[0].toFlattenedJson()).toEqual(token);
    });
  });

  describe('hasSkipToken()', () => {
    it('should indicate whether a skip token was returned', async () => {
      expect(response.hasSkipToken()).toEqual(true);
    });
  });

  describe('getSkipToken()', () => {
    it('should return the skip token', async () => {
      expect(response.getSkipToken()).toEqual('abc');
    });
  });

});
