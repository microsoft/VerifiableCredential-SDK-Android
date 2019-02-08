import { HubErrorCode } from '@decentralized-identity/hub-common-js';
import HubError from '../src/HubError';

const hubErrorBody = {
  error_code: HubErrorCode.NotFound,
  target: 'example',
};

const hubError = new HubError(hubErrorBody);

describe('HubError', () => {

  describe('is', () => {
    it('should indicate whether an object is a HubError', async () => {
      expect(HubError.is(hubError)).toBeTruthy();
      expect(HubError.is(new Error())).toBeFalsy();
    });
  });

  describe('constructor', () => {
    it('should fix the prototype chain', async () => {
      expect(hubError instanceof HubError).toBeTruthy();
      expect(hubError instanceof Error).toBeTruthy();
    });
  });

  describe('getErrorCode()', () => {
    it('should return the error code', async () => {
      expect(hubError.getErrorCode()).toEqual(hubErrorBody.error_code);
    });
  });

  describe('getTarget()', () => {
    it('should return the taret', async () => {
      expect(hubError.getTarget()).toEqual(hubErrorBody.target);
    });
  });

  describe('getRawError()', () => {
    it('should return the raw error body', async () => {
      expect(hubError.getRawError()).toEqual(hubErrorBody);
    });
  });

});
