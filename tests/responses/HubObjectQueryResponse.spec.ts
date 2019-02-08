import { IObjectMetadata } from '@decentralized-identity/hub-common-js';
import HubObjectQueryResponse from '../../src/responses/HubObjectQueryResponse';

const objects: IObjectMetadata[] = [{
  interface: 'Collections',
  context: 'schema.org',
  type: 'MusicPlaylist',
  id: 'abc',
  created_by: 'did:test:example.id',
  created_at: '2019-01-01',
  sub: 'did:test:example.id',
  commit_strategy: 'basic',
}];

const response = new HubObjectQueryResponse({
  '@context': 'https://schema.identity.foundation/0.1',
  '@type': 'ObjectQueryResponse',
  objects,
  skip_token: 'abc',
});

describe('HubObjectQueryResponse', () => {

  describe('constructor', () => {
    it('should throw on an invalid response type', async () => {
      try {
        const r = new HubObjectQueryResponse({
          '@type': 'WrongType',
        } as any);
        fail('Constructor was expected to throw');
      } catch (e) {
        // Expected
      }
    });
  });

  describe('getObjects()', () => {
    it('should return the matching objects', async () => {
      const returnedObjects = await response.getObjects();
      expect(returnedObjects).toEqual(objects);
    });

    it('should return an array even if none was in the response', async () => {
      const response = new HubObjectQueryResponse({
        '@context': 'https://schema.identity.foundation/0.1',
        '@type': 'ObjectQueryResponse',
        objects: null,
      } as any);
      expect(Array.isArray(response.getObjects())).toEqual(true);
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
