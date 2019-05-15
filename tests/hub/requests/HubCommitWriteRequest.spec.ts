import HubCommitWriteRequest from '../../../src/hub/requests/HubCommitWriteRequest';
import SignedCommit from '../../../src/hub/SignedCommit';

describe('HubWriteRequest', () => {

  describe('getRequestJson()', () => {
    it('should return a complete request body', async () => {

      const flattenedCommitJson = {
        protected: 'test',
        payload: 'test',
        signature: 'test',
      };

      const req = new HubCommitWriteRequest(new SignedCommit(flattenedCommitJson));

      const json = await req.getRequestJson();

      expect(json).toEqual({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'WriteRequest',
        commit: flattenedCommitJson,
      });

    });

  });
});
