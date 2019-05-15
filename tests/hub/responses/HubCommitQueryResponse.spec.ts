import HubCommitQueryResponse from '../../../src/hub/responses/HubCommitQueryResponse';
import SignedCommit from '../../../src/hub/SignedCommit';

const flattenedCommitJson = {
  protected: 'test',
  payload: 'test',
  signature: 'test',
};

describe('HubCommitQueryResponse', () => {

  let response: HubCommitQueryResponse;

  beforeAll(() => {
    response = new HubCommitQueryResponse({
      '@context': 'https://schema.identity.foundation/0.1',
      '@type': 'CommitQueryResponse',
      commits: [flattenedCommitJson],
      skip_token: 'abc',
    });
  });

  describe('constructor', () => {
    it('should throw on an invalid response type', async () => {
      try {
        const r = new HubCommitQueryResponse(<any> {
          '@type': 'WrongType',
        });
        fail('Constructor was expected to throw');
      } catch (e) {
        // Expected
      }
    });
  });

  describe('getCommits()', () => {
    it('should return the matching commits', async () => {
      const returnedCommits = await response.getCommits();

      expect(returnedCommits.length).toEqual(1);
      expect(returnedCommits[0].toFlattenedJson()).toEqual(flattenedCommitJson);
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
