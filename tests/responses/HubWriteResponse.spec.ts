import HubWriteResponse from '../../src/responses/HubWriteResponse';

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
