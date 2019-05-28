
import { Crypto } from '@peculiar/webcrypto';
import DidKey from '../lib/DidKey';
import { KeyExport } from '../lib/KeyExport';
const pairwiseKeys = require('./Pairwise.RSA.json');

const crypto = new Crypto();
const seed = Buffer.from('xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi');
describe('DidKey - RSA pairwise keys', () => {

  let originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;

  beforeEach(() => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 20000;
  });

  afterEach(() => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
  });

  it('should generate unique pairwise identifiers using a different peer', async (done) => {
    const nrIds: number = 10;
    const did: string = 'abcdef';
    const alg = { name: 'RSASSA-PKCS1-v1_5', modulusLength: 1024, publicExponent: new Uint8Array([0x01, 0x00, 0x01]), hash: { name: 'SHA-256' } };
    for (let index = 0; index < nrIds; index++) {
      const didKey: DidKey = new DidKey(crypto, alg, null);
      const id = `${index}`;
      const pairwiseKey: DidKey = await didKey.generatePairwise(seed, did, id);
      const jwk = await pairwiseKey.getJwkKey(KeyExport.Private);
      // The following comments is used to generate a test vector reference file. Do not remove.
      console.log(`{ "pwid": "${id}", "key": "${jwk.d}"},`);
      expect(jwk.kid).toBeDefined();

      expect(pairwiseKeys[index].key).toBe(jwk.d);
      expect(1).toBe(pairwiseKeys.filter((element: any) => element.key === jwk.d).length);
    }
    done();
  });
});
