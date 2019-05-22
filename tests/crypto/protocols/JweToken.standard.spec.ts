//import base64url from "base64url";
import JweToken from "../../../src/crypto/protocols/jwe/JweToken";
import { IEncryptionOptions } from "../../../src/crypto/keyStore/IKeyStore";
import KeyStoreMem from '../../../src/crypto/keyStore/KeyStoreMem';
import CryptoFactory from '../../../src/crypto/plugin/CryptoFactory';
import DefaultCryptoSuite from '../../../src/crypto/plugin/DefaultCryptoSuite';
import { ProtectionFormat } from '../../../src/crypto/keyStore/ProtectionFormat';
import RsaPrivateKey from '../../../src/crypto/keys/rsa/RsaPrivateKey';
import { KeyOperation } from '../../../src/crypto/keys/PublicKey';
import JoseHelpers from '../../../src/crypto/protocols/jose/JoseHelpers';
import base64url from 'base64url';
import nodeWebcryptoOssl from 'node-webcrypto-ossl';

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

describe('JweToken standard', () => {
  it('should run RFC 7516 A.1.  Example JWE using RSAES-OAEP and AES GCM', async () => {
    const payload = 'The true sign of intelligence is not knowledge but imagination.';
    const contentEncryptionKey = [177, 161, 244, 128, 84, 143, 225, 115, 63, 180, 3, 255, 107, 154,
      212, 246, 138, 7, 110, 91, 112, 46, 34, 105, 47, 130, 203, 46, 122,
      234, 64, 252];
    const iv = [227, 197, 117, 252, 2, 219, 233, 68, 180, 225, 77, 219];
      const keyStore = new KeyStoreMem();
      const cryptoSuite = new DefaultCryptoSuite();
      const options: IEncryptionOptions = {
        cryptoFactory: new CryptoFactory(keyStore, cryptoSuite),
        contentEncryptionAlgorithm: 'A256GCM',
        contentEncryptionKey: Buffer.from(contentEncryptionKey),
        initialVector: Buffer.from(iv) 
      };

      const key = new RsaPrivateKey();
      key.alg = 'RSA-OAEP';
      key.e = 'AQAB';
      key.n = 'oahUIoWw0K0usKNuOR6H4wkf4oBUXHTxRvgb48E-BVvxkeDNjbC4he8rUWcJoZmds2h7M70imEVhRU5djINXtqllXI4DFqcI1DgjT9LewND8MW2Krf3Spsk_ZkoFnilakGygTwpZ3uesH-PFABNIUYpOiN15dsQRkgr0vEhxN92i2asbOenSZeyaxziK72UwxrrKoExv6kc5twXTq4h-QChLOln0_mtUZwfsRaMStPs6mS6XrgxnxbWhojf663tuEQueGC-FCMfra36C9knDFGzKsNa7LZK2djYgyD3JR_MB_4NUJW_TqOQtwHYbxevoJArm-L5StowjzGy-_bq6Gw';
      key.d = 'kLdtIj6GbDks_ApCSTYQtelcNttlKiOyPzMrXHeI-yk1F7-kpDxY4-WY5NWV5KntaEeXS1j82E375xxhWMHXyvjYecPT9fpwR_M9gV8n9Hrh2anTpTD93Dt62ypW3yDsJzBnTnrYu1iwWRgBKrEYY46qAZIrA2xAwnm2X7uGR1hghkqDp0Vqj3kbSCz1XyfCs6_LehBwtxHIyh8Ripy40p24moOAbgxVw3rxT_vlt3UVe4WO3JkJOzlpUf-KTVI2Ptgm-dARxTEtE-id-4OJr0h-K-VFs3VSndVTIznSxfyrj8ILL6MG_Uv8YAu7VILSB3lOW085-4qE3DzgrTjgyQ';
      key.p = '1r52Xk46c-LsfB5P442p7atdPUrxQSy4mti_tZI3Mgf2EuFVbUoDBvaRQ-SWxkbkmoEzL7JXroSBjSrK3YIQgYdMgyAEPTPjXv_hI2_1eTSPVZfzL0lffNn03IXqWF5MDFuoUYE0hzb2vhrlN_rKrbfDIwUbTrjjgieRbwC6Cl0';
      key.q = 'wLb35x7hmQWZsWJmB_vle87ihgZ19S8lBEROLIsZG4ayZVe9Hi9gDVCOBmUDdaDYVTSNx_8Fyw1YYa9XGrGnDew00J28cRUoeBB_jKI1oma0Orv1T9aXIWxKwd4gvxFImOWr3QRL9KEBRzk2RatUBnmDZJTIAfwTs0g68UZHvtc';
      key.dp = 'ZK-YwE7diUh0qR1tR7w8WHtolDx3MZ_OTowiFvgfeQ3SiresXjm9gZ5KLhMXvo-uz-KUJWDxS5pFQ_M0evdo1dKiRTjVw_x4NyqyXPM5nULPkcpU827rnpZzAJKpdhWAgqrXGKAECQH0Xt4taznjnd_zVpAmZZq60WPMBMfKcuE';
      key.dq = 'Dq0gfgJ1DdFGXiLvQEZnuKEN0UUmsJBxkjydc3j4ZYdBiMRAy86x0vHCjywcMlYYg4yoC4YZa9hNVcsjqA3FeiL19rk8g6Qn29Tt0cj8qqyFpz9vNDBUfCAiJVeESOjJDZPYHdHY8v1b-o-Z2X5tvLx-TCekf7oxyeKDUqKWjis';
      key.qi = 'VIMpMYbPf47dT1w_zDUXfPimsSegnMOA1zTaX7aGk_8urY6R8-ZW1FxU7AlWAyLWybqq6t16VFd7hQd0y6flUK4SlOydB61gwanOsXGOAOv82cHq0E3eL4HrtZkUuKvnPrMnsUUFlfUdybVzxyjz9JF_XyaY14ardLSjf4L_FNY';
      key.key_ops = [KeyOperation.Verify];

      await keyStore.save('key', key);
      const jweToken = new JweToken(options);
      const cipher = await jweToken.encrypt([key.getPublicKey()], payload, ProtectionFormat.JweCompactJson);
      expect(JoseHelpers.encodeHeader(cipher.protected)).toEqual('eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ');
      expect(base64url.encode(cipher.iv)).toEqual('48V1_ALb6US04U3b');
      expect(base64url.encode(cipher.aad)).toEqual('eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZHQ00ifQ');
      expect(base64url.encode(cipher.tag)).toEqual('XFBoMYUZodetZdvTiFvSkQ');
      expect(base64url.encode(cipher.ciphertext)).toEqual('5eym8TW_c8SuK0ltJ3rpYIzOeDQz7TALvtu6UG9oMo4vpzs9tX_EFShS8iB7j6jiSdiwkIr3ajwQzaBtQD_A');

      const jwk =    {   //this is an example jwk key, "raw" would be an ArrayBuffer
      kty: "oct",
      k: base64url.encode( Buffer.from(contentEncryptionKey)),
      alg: "A256GCM",
      ext: true,
      };
      const aad =  [101, 121, 74, 104, 98, 71, 99, 105, 79, 105, 74, 83, 85, 48, 69,
        116, 84, 48, 70, 70, 85, 67, 73, 115, 73, 109, 86, 117, 89, 121, 73,
        54, 73, 107, 69, 121, 78, 84, 90, 72, 81, 48, 48, 105, 102, 81];
      const crypto = new nodeWebcryptoOssl();

      const aesKey = await crypto.subtle.importKey(
        "jwk", //can be "jwk" or "raw"
        jwk,
        <RsaHashedImportParams>{
          name: "AES-GCM",
        },
        true, //whether the key is extractable (i.e. can be used in exportKey)
        ["decrypt"] //can "encrypt", "decrypt", "wrapKey", or "unwrapKey"          
        );

        const aesEncrypt = {
          name: "AES-GCM",
          iv: new Uint8Array(iv),
          additionalData: new Uint8Array(aad),
          tagLength: 128, //The tagLength you used to encrypt (if any)
      };
      const inputcipher =  new Uint8Array(Buffer.concat([cipher.ciphertext, cipher.tag]));
        const plain = await crypto.subtle.decrypt(
          aesEncrypt,
          aesKey,
          inputcipher);
        // const text = String.fromCharCode.apply(null, plain);
        expect(plain).toBeDefined();
        

      // Decrypt
      const plaintext = await cipher.decrypt('key');
      expect(plaintext).toEqual(Buffer.from(payload));
    });

        it('should add kid and default alg', async () => {
          const payload = 'The true sign of intelligence is not knowledge but imagination.';
          const contentEncryptionKey = [177, 161, 244, 128, 84, 143, 225, 115, 63, 180, 3, 255, 107, 154,
            212, 246, 138, 7, 110, 91, 112, 46, 34, 105, 47, 130, 203, 46, 122,
            234, 64, 252];
          const iv = [227, 197, 117, 252, 2, 219, 233, 68, 180, 225, 77, 219];
            const keyStore = new KeyStoreMem();
            const cryptoSuite = new DefaultCryptoSuite();
            const options: IEncryptionOptions = {
              cryptoFactory: new CryptoFactory(keyStore, cryptoSuite),
              contentEncryptionAlgorithm: 'A256GCM',
              contentEncryptionKey: Buffer.from(contentEncryptionKey),
              initialVector: Buffer.from(iv) 
            };
      
            const key = new RsaPrivateKey();
            key.alg = '';
            key.kid = 'key1';
            key.e = 'AQAB';
            key.n = 'oahUIoWw0K0usKNuOR6H4wkf4oBUXHTxRvgb48E-BVvxkeDNjbC4he8rUWcJoZmds2h7M70imEVhRU5djINXtqllXI4DFqcI1DgjT9LewND8MW2Krf3Spsk_ZkoFnilakGygTwpZ3uesH-PFABNIUYpOiN15dsQRkgr0vEhxN92i2asbOenSZeyaxziK72UwxrrKoExv6kc5twXTq4h-QChLOln0_mtUZwfsRaMStPs6mS6XrgxnxbWhojf663tuEQueGC-FCMfra36C9knDFGzKsNa7LZK2djYgyD3JR_MB_4NUJW_TqOQtwHYbxevoJArm-L5StowjzGy-_bq6Gw';
            key.d = 'kLdtIj6GbDks_ApCSTYQtelcNttlKiOyPzMrXHeI-yk1F7-kpDxY4-WY5NWV5KntaEeXS1j82E375xxhWMHXyvjYecPT9fpwR_M9gV8n9Hrh2anTpTD93Dt62ypW3yDsJzBnTnrYu1iwWRgBKrEYY46qAZIrA2xAwnm2X7uGR1hghkqDp0Vqj3kbSCz1XyfCs6_LehBwtxHIyh8Ripy40p24moOAbgxVw3rxT_vlt3UVe4WO3JkJOzlpUf-KTVI2Ptgm-dARxTEtE-id-4OJr0h-K-VFs3VSndVTIznSxfyrj8ILL6MG_Uv8YAu7VILSB3lOW085-4qE3DzgrTjgyQ';
            key.p = '1r52Xk46c-LsfB5P442p7atdPUrxQSy4mti_tZI3Mgf2EuFVbUoDBvaRQ-SWxkbkmoEzL7JXroSBjSrK3YIQgYdMgyAEPTPjXv_hI2_1eTSPVZfzL0lffNn03IXqWF5MDFuoUYE0hzb2vhrlN_rKrbfDIwUbTrjjgieRbwC6Cl0';
            key.q = 'wLb35x7hmQWZsWJmB_vle87ihgZ19S8lBEROLIsZG4ayZVe9Hi9gDVCOBmUDdaDYVTSNx_8Fyw1YYa9XGrGnDew00J28cRUoeBB_jKI1oma0Orv1T9aXIWxKwd4gvxFImOWr3QRL9KEBRzk2RatUBnmDZJTIAfwTs0g68UZHvtc';
            key.dp = 'ZK-YwE7diUh0qR1tR7w8WHtolDx3MZ_OTowiFvgfeQ3SiresXjm9gZ5KLhMXvo-uz-KUJWDxS5pFQ_M0evdo1dKiRTjVw_x4NyqyXPM5nULPkcpU827rnpZzAJKpdhWAgqrXGKAECQH0Xt4taznjnd_zVpAmZZq60WPMBMfKcuE';
            key.dq = 'Dq0gfgJ1DdFGXiLvQEZnuKEN0UUmsJBxkjydc3j4ZYdBiMRAy86x0vHCjywcMlYYg4yoC4YZa9hNVcsjqA3FeiL19rk8g6Qn29Tt0cj8qqyFpz9vNDBUfCAiJVeESOjJDZPYHdHY8v1b-o-Z2X5tvLx-TCekf7oxyeKDUqKWjis';
            key.qi = 'VIMpMYbPf47dT1w_zDUXfPimsSegnMOA1zTaX7aGk_8urY6R8-ZW1FxU7AlWAyLWybqq6t16VFd7hQd0y6flUK4SlOydB61gwanOsXGOAOv82cHq0E3eL4HrtZkUuKvnPrMnsUUFlfUdybVzxyjz9JF_XyaY14ardLSjf4L_FNY';
            key.key_ops = [KeyOperation.Verify];
      
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