import HubCommitQueryRequest from '../../src/requests/HubCommitQueryRequest';

describe('HubCommitQueryRequest', () => {

  describe('getRequestJson()', () => {
    it('should return a complete request body', async () => {

      const req = new HubCommitQueryRequest({
        object_id: ['1234'],
      });

      const json = await req.getRequestJson();

      expect(json).toEqual({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'CommitQueryRequest',
        query: {
          object_id: ['1234'],
        },
      });

    });

  });
});
