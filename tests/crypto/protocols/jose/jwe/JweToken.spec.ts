/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
 import JweToken from "../../../../../src/crypto/protocols/jose/jwe/JweToken";
 import KeyStoreInMemory from '../../../../../src/crypto/keyStore/KeyStoreInMemory';
 import CryptoFactory from '../../../../../src/crypto/plugin/CryptoFactory';
 import SubtleCryptoNodeOperations from '../../../../../src/crypto/plugin/SubtleCryptoNodeOperations';
 import { ProtectionFormat } from '../../../../../src/crypto/keyStore/ProtectionFormat';
 import RsaPrivateKey from '../../../../../src/crypto/keys/rsa/RsaPrivateKey';
 import { KeyOperation } from '../../../../../src/crypto/keys/PublicKey';
 import JoseHelpers from '../../../../../src/crypto/protocols/jose/JoseHelpers';
 import base64url from 'base64url';
import { IJweEncryptionOptions } from "../../../../../src/crypto/protocols/jose/IJoseOptions";
import IPayloadProtectionProtocolOptions from "../../../../../src/crypto/protocols/IPayloadProtectionProtocolOptions";
import { TSMap } from "typescript-map";
import JoseProtocol from "../../../../../src/crypto/protocols/jose/JoseProtocol";
import JoseConstants from "../../../../../src/crypto/protocols/jose/JoseConstants";
import { SubtleCryptoExtension, SecretKey } from "../../../../../src";
 
describe('JweToken', () => {
  it('should create, decrypt and serialize a JweToken', async () => {
    const payload = 'The true sign of intelligence is not knowledge but imagination.';      
    const keyStore = new KeyStoreInMemory();
    await keyStore.save('seed', new SecretKey('ABEE'));
    const cryptoFactory = new CryptoFactory(keyStore, new SubtleCryptoNodeOperations())
    const options: IPayloadProtectionProtocolOptions = {
        cryptoFactory: cryptoFactory,
        protocolOption: new TSMap<string, any>(),
        protocolInterface: new JoseProtocol()
    };

    options.protocolOption.set(JoseConstants.optionContentEncryptionAlgorithm, 'A256GCM');
    
    const alg = { name: 'RSA-OAEP', hash: 'SHA-256', modulusLength: 2048, publicExponent: new Uint8Array([0x01, 0x00, 0x01]) };
    const generator = new SubtleCryptoExtension(cryptoFactory);
    const privateKey = await generator.generatePairwiseKey(alg, 'seed', 'persona','peer');
    await keyStore.save('key', privateKey);

    const cipher = await options.protocolInterface.encrypt([await keyStore.get('key', true)], Buffer.from(payload), 'JweGeneralJson', options);
    expect(cipher.get(JoseConstants.tokenAad)).toBeDefined();
    expect(cipher.get(JoseConstants.tokenCiphertext)).toBeDefined();
    expect(cipher.get(JoseConstants.tokenFormat)).toBe(ProtectionFormat.JweGeneralJson);
    expect(cipher.get(JoseConstants.tokenIv)).toBeDefined();
    expect(cipher.get(JoseConstants.tokenProtected)).toBeDefined();
    expect(cipher.get(JoseConstants.tokenRecipients)).toBeDefined();
    expect(cipher.get(JoseConstants.tokenPayload)).toBeUndefined();

    // serialize
    let serialized = options.protocolInterface.serialize(cipher, 'JweGeneralJson', options);
    let parsed = JSON.parse(serialized);
    expect(parsed['aad']).toBeDefined();
    expect(parsed['ciphertext']).toBeDefined();
    expect(parsed['iv']).toBeDefined();
    expect(parsed['protected']).toBeDefined();
    expect(parsed['recipients']).toBeDefined();
    expect(parsed['tag']).toBeDefined();
    expect(parsed[JoseConstants.tokenPayload]).toBeUndefined();

    // serialize
    let deserialized = options.protocolInterface.deserialize(serialized, 'JweGeneralJson', options);
    expect(deserialized.get(JoseConstants.tokenAad)).toEqual(cipher.get(JoseConstants.tokenAad));
    expect(deserialized.get(JoseConstants.tokenCiphertext)).toEqual(cipher.get(JoseConstants.tokenCiphertext));
    expect(deserialized.get(JoseConstants.tokenFormat)).toEqual(ProtectionFormat.JweGeneralJson);
    expect(deserialized.get(JoseConstants.tokenIv)).toEqual(cipher.get(JoseConstants.tokenIv));
    expect(deserialized.get(JoseConstants.tokenProtected)).toEqual(cipher.get(JoseConstants.tokenProtected));
    expect(deserialized.get(JoseConstants.tokenRecipients)).toEqual(cipher.get(JoseConstants.tokenRecipients));

    // decrypt
    const decrypted = await options.protocolInterface.decrypt('key', deserialized, options);
    expect(decrypted).toEqual(Buffer.from(payload));

    // Flat serialization
    serialized = options.protocolInterface.serialize(cipher, 'JweFlatJson', options);
    parsed = JSON.parse(serialized);
    expect(parsed['aad']).toBeDefined();
    expect(parsed['ciphertext']).toBeDefined();
    expect(parsed['iv']).toBeDefined();
    expect(parsed['protected']).toBeDefined();
    expect(parsed['encrypted_key']).toBeDefined();
    expect(parsed['recipients']).toBeUndefined();
    expect(parsed['tag']).toBeDefined();
    expect(parsed[JoseConstants.tokenPayload]).toBeUndefined();

    deserialized = options.protocolInterface.deserialize(serialized, 'JweFlatJson', options);
    expect(deserialized.get(JoseConstants.tokenAad)).toEqual(cipher.get(JoseConstants.tokenAad));
    expect(deserialized.get(JoseConstants.tokenCiphertext)).toEqual(cipher.get(JoseConstants.tokenCiphertext));
    expect(deserialized.get(JoseConstants.tokenFormat)).toEqual(ProtectionFormat.JweFlatJson);
    expect(deserialized.get(JoseConstants.tokenIv)).toEqual(cipher.get(JoseConstants.tokenIv));
    expect(deserialized.get(JoseConstants.tokenProtected)).toEqual(cipher.get(JoseConstants.tokenProtected));
    expect(deserialized.get(JoseConstants.tokenRecipients)).toEqual(cipher.get(JoseConstants.tokenRecipients));

    // Compact serialization
    serialized = options.protocolInterface.serialize(cipher, 'JweCompactJson', options);
    parsed = serialized.split('.');
    expect(parsed.length).toEqual(5);

    deserialized = options.protocolInterface.deserialize(serialized, 'JweCompactJson', options);
    expect(deserialized.get(JoseConstants.tokenAad)).toEqual(cipher.get(JoseConstants.tokenAad));
    expect(deserialized.get(JoseConstants.tokenCiphertext)).toEqual(cipher.get(JoseConstants.tokenCiphertext));
    expect(deserialized.get(JoseConstants.tokenFormat)).toEqual(ProtectionFormat.JweCompactJson);
    expect(deserialized.get(JoseConstants.tokenIv)).toEqual(cipher.get(JoseConstants.tokenIv));
    expect(deserialized.get(JoseConstants.tokenProtected)).toEqual(cipher.get(JoseConstants.tokenProtected));
    expect(deserialized.get(JoseConstants.tokenRecipients)).toEqual(cipher.get(JoseConstants.tokenRecipients));
    });

    it('should add kid and default alg', async () => {
          const payload = 'The true sign of intelligence is not knowledge but imagination.';
          const contentEncryptionKey = [177, 161, 244, 128, 84, 143, 225, 115, 63, 180, 3, 255, 107, 154,
            212, 246, 138, 7, 110, 91, 112, 46, 34, 105, 47, 130, 203, 46, 122,
            234, 64, 252];
          const iv = [227, 197, 117, 252, 2, 219, 233, 68, 180, 225, 77, 219];
            const keyStore = new KeyStoreInMemory();
            const cryptoSuite = new SubtleCryptoNodeOperations();
            const options: IJweEncryptionOptions = {
              cryptoFactory: new CryptoFactory(keyStore, cryptoSuite),
              contentEncryptionAlgorithm: 'A256GCM',
              contentEncryptionKey: Buffer.from(contentEncryptionKey),
              initialVector: Buffer.from(iv) 
            };
      
            const privateKey = {
              alg: '',
              kid: 'key1',
              e: 'AQAB',
              n: 'oahUIoWw0K0usKNuOR6H4wkf4oBUXHTxRvgb48E-BVvxkeDNjbC4he8rUWcJoZmds2h7M70imEVhRU5djINXtqllXI4DFqcI1DgjT9LewND8MW2Krf3Spsk_ZkoFnilakGygTwpZ3uesH-PFABNIUYpOiN15dsQRkgr0vEhxN92i2asbOenSZeyaxziK72UwxrrKoExv6kc5twXTq4h-QChLOln0_mtUZwfsRaMStPs6mS6XrgxnxbWhojf663tuEQueGC-FCMfra36C9knDFGzKsNa7LZK2djYgyD3JR_MB_4NUJW_TqOQtwHYbxevoJArm-L5StowjzGy-_bq6Gw',
              d: 'kLdtIj6GbDks_ApCSTYQtelcNttlKiOyPzMrXHeI-yk1F7-kpDxY4-WY5NWV5KntaEeXS1j82E375xxhWMHXyvjYecPT9fpwR_M9gV8n9Hrh2anTpTD93Dt62ypW3yDsJzBnTnrYu1iwWRgBKrEYY46qAZIrA2xAwnm2X7uGR1hghkqDp0Vqj3kbSCz1XyfCs6_LehBwtxHIyh8Ripy40p24moOAbgxVw3rxT_vlt3UVe4WO3JkJOzlpUf-KTVI2Ptgm-dARxTEtE-id-4OJr0h-K-VFs3VSndVTIznSxfyrj8ILL6MG_Uv8YAu7VILSB3lOW085-4qE3DzgrTjgyQ',
              p: '1r52Xk46c-LsfB5P442p7atdPUrxQSy4mti_tZI3Mgf2EuFVbUoDBvaRQ-SWxkbkmoEzL7JXroSBjSrK3YIQgYdMgyAEPTPjXv_hI2_1eTSPVZfzL0lffNn03IXqWF5MDFuoUYE0hzb2vhrlN_rKrbfDIwUbTrjjgieRbwC6Cl0',
              q: 'wLb35x7hmQWZsWJmB_vle87ihgZ19S8lBEROLIsZG4ayZVe9Hi9gDVCOBmUDdaDYVTSNx_8Fyw1YYa9XGrGnDew00J28cRUoeBB_jKI1oma0Orv1T9aXIWxKwd4gvxFImOWr3QRL9KEBRzk2RatUBnmDZJTIAfwTs0g68UZHvtc',
              dp: 'ZK-YwE7diUh0qR1tR7w8WHtolDx3MZ_OTowiFvgfeQ3SiresXjm9gZ5KLhMXvo-uz-KUJWDxS5pFQ_M0evdo1dKiRTjVw_x4NyqyXPM5nULPkcpU827rnpZzAJKpdhWAgqrXGKAECQH0Xt4taznjnd_zVpAmZZq60WPMBMfKcuE',
              dq: 'Dq0gfgJ1DdFGXiLvQEZnuKEN0UUmsJBxkjydc3j4ZYdBiMRAy86x0vHCjywcMlYYg4yoC4YZa9hNVcsjqA3FeiL19rk8g6Qn29Tt0cj8qqyFpz9vNDBUfCAiJVeESOjJDZPYHdHY8v1b-o-Z2X5tvLx-TCekf7oxyeKDUqKWjis',
              qi: 'VIMpMYbPf47dT1w_zDUXfPimsSegnMOA1zTaX7aGk_8urY6R8-ZW1FxU7AlWAyLWybqq6t16VFd7hQd0y6flUK4SlOydB61gwanOsXGOAOv82cHq0E3eL4HrtZkUuKvnPrMnsUUFlfUdybVzxyjz9JF_XyaY14ardLSjf4L_FNY',
              key_ops: [KeyOperation.Verify]  
            }
            const key = new RsaPrivateKey(privateKey);
      
            await keyStore.save('key', key);
            const jweToken = new JweToken(options);
            const cipher = await jweToken.encrypt([key.getPublicKey()], payload, ProtectionFormat.JweCompactJson);
            expect(JoseHelpers.encodeHeader(cipher.protected)).toEqual('eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIiwia2lkIjoia2V5MSJ9');
            expect(base64url.encode(cipher.iv)).toEqual('48V1_ALb6US04U3b');
            expect(base64url.encode(cipher.aad)).toEqual('eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIiwia2lkIjoia2V5MSJ9');
            expect(base64url.encode(cipher.tag)).toEqual('eU_zskwUtrjl6qNjeEgtAQ');
            expect(base64url.encode(cipher.ciphertext)).toEqual('5eym8TW_c8SuK0ltJ3rpYIzOeDQz7TALvtu6UG9oMo4vpzs9tX_EFShS8iB7j6jiSdiwkIr3ajwQzaBtQD_A');

            // Decrypt
            const plaintext = await cipher.decrypt('key');
            expect(plaintext).toEqual(Buffer.from(payload));
        });
        
});
