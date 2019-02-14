import HubCommitQueryResponse from '../../src/responses/HubCommitQueryResponse';
import SignedCommit from '../../src/SignedCommit';

const flattenedCommitJson = {
  protected: 'test',
  payload: 'test',
  signature: 'test',
};

const response = new HubCommitQueryResponse({
  '@context': 'https://schema.identity.foundation/0.1',
  '@type': 'CommitQueryResponse',
  commits: [flattenedCommitJson],
  skip_token: 'abc',
});

describe('HubCommitQueryResponse', () => {

  describe('constructor', () => {
    it('should throw on an invalid response type', async () => {
      try {
        const r = new HubCommitQueryResponse({
          '@type': 'WrongType',
        } as any);
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
