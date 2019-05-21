
import KeyStoreMem from '../../../src/crypto/keyStore/KeyStoreMem';
import RsaPublicKey from '../../../src/crypto/keys/rsa/RsaPublicKey';
import { KeyType } from '../../../src/crypto/keys/KeyType';
import PublicKey from '../../../src/crypto/keys/PublicKey';

describe('KeyStoreMem', () => {

  it('should list all keys in the store', async () => {
    const keyStore = new KeyStoreMem();
    const key1: RsaPublicKey = {
      kty: KeyType.RSA,
      kid: 'kid1',
      e: 'AAEE',
      n: 'xxxxxxxxx',
      alg: 'none'
    };
    const key2: RsaPublicKey = {
      kty: KeyType.RSA,
      kid: 'kid2',
      e: 'AAEE',
      n: 'xxxxxxxxx',
      alg: 'none'
    };
    await keyStore.save('1', <PublicKey>key1);
    await keyStore.save('2', <PublicKey>key2);
    let list = await keyStore.list();
// tslint:disable-next-line: no-backbone-get-set-outside-model
    expect(list.get('1')).toBe('kid1');
// tslint:disable-next-line: no-backbone-get-set-outside-model
    expect(list.get('2')).toBe('kid2'); 
  });

  it('should throw because an oct key does not have a public key', async () => {

    // Setup registration environment
    const jwk: any = {
      kty: 'oct',
      use: 'sig',
      k: 'AAEE'
    };

    const keyStore = new KeyStoreMem();
    await keyStore.save('key', jwk);
    let throwCaught = false;
    const signature = await keyStore.get('key', true)
    .catch((err) => {
      throwCaught = true;
      expect(err.message).toBe('A secret does not has a public key');
    });
    expect(signature).toBeUndefined();
    expect(throwCaught).toBe(true);
  });
});
