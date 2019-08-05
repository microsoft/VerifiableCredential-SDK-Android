/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubObjectQueryRequest from '../../../src/hubSession/requests/HubObjectQueryRequest';

describe('HubObjectQueryRequest', () => {

  describe('getRequestJson()', () => {
    it('should return a complete request body', async () => {

      const req = new HubObjectQueryRequest({
        object_id: ['1234'],
      });

      const json = await req.getRequestJson();

      expect(json).toEqual({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'ObjectQueryRequest',
        query: {
          object_id: ['1234'],
        },
      });

    });

  });
});
