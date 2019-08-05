/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubWriteResponse from '../../../src/hubSession/responses/HubWriteResponse';

describe('HubWriteResponse', () => {

  describe('getRevisions()', () => {
    it('should return the object revisions', async () => {

      const resp = new HubWriteResponse({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'WriteResponse',
        revisions: ['abc', 'def'],
      });

      expect(await resp.getRevisions()).toEqual(['abc', 'def']);

    });

  });
});
