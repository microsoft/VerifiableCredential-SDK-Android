import HubObjectQueryRequest from '../../src/requests/HubObjectQueryRequest';

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
