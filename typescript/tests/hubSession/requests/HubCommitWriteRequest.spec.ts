/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubCommitWriteRequest from '../../../src/hubSession/requests/HubCommitWriteRequest';
import JoseToken from '../../../src/crypto/protocols/jose/JoseToken';
import { ProtectionFormat } from '../../../src/crypto/keyStore/ProtectionFormat';
import JoseConstants from '../../../src/crypto/protocols/jose/JoseConstants';
import IPayloadProtectionOptions from '../../../src/crypto/protocols/IPayloadProtectionOptions';
import ProtocolTest from '../../crypto/protocols/jose/ProtocolTest';
import { TSMap } from 'typescript-map';
import JoseProtocol from '../../../src/crypto/protocols/jose/JoseProtocol';

describe('HubWriteRequest', () => {

  describe('getRequestJson()', () => {
    it('should return a complete request body', async () => {
      const  options = <any> {
        payloadProtection: new JoseProtocol()
      };
      const protectedHeader = new TSMap();
      const flattenedCommitJson = new JoseToken(options, new ProtocolTest(), [
        [JoseConstants.tokenFormat, ProtectionFormat.JwsFlatJson],
        [JoseConstants.tokenPayload, 'test'],
        [JoseConstants.tokenProtected, 'test'],
        [JoseConstants.tokenSignatures, [{signature: 'test',protected: protectedHeader}]]]);

      const req = new HubCommitWriteRequest(flattenedCommitJson);

      const json = await req.getRequestJson();

      expect(json).toEqual({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'WriteRequest',
        commit: '{"format":"JwsFlatJson","payload":"test","protected":"test","signatures":[{"signature":"test","protected":{}}]}',
      });

    });

  });
});
