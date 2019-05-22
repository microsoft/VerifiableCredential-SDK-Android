import base64url from "base64url";
import JwsToken from "../../../src/crypto/protocols/jws/JwsToken";
import { ISigningOptions } from "../../../src/crypto/keyStore/IKeyStore";
import KeyStoreMem from "../../../src/crypto/keyStore/KeyStoreMem";
import CryptoFactory from "../../../src/crypto/plugin/CryptoFactory";
import DefaultCryptoSuite from "../../../src/crypto/plugin/DefaultCryptoSuite";
import { ProtectionFormat } from "../../../src/crypto/keyStore/ProtectionFormat";
import RsaPrivateKey from "../../../src/crypto/keys/rsa/RsaPrivateKey";
import { KeyOperation } from "../../../src/crypto/keys/PublicKey";

/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

describe('JwsToken standard', () => {
  it('should run RFC 7515 A.2.  Example JWS Using RSASSA-PKCS1-v1_5 SHA-256', async () => {
    const payload = '{"iss":"joe",\r\n'+
    ' "exp":1300819380,\r\n'+
    ' "http://example.com/is_root":true}';
    const payloadBuffer = Buffer.from([123, 34, 105, 115, 115, 34, 58, 34, 106, 111, 101, 34, 44, 13, 10,
      32, 34, 101, 120, 112, 34, 58, 49, 51, 48, 48, 56, 49, 57, 51, 56, 48, 44, 13, 10, 
      32, 34, 104, 116, 116, 112, 58, 47, 47, 101, 120, 97,
      109, 112, 108, 101, 46, 99, 111, 109, 47, 105, 115, 95, 114, 111,
      111, 116, 34, 58, 116, 114, 117, 101, 125]);
      expect(payload).toBeDefined();
      expect(payloadBuffer).toBeDefined();
    
      const keyStore = new KeyStoreMem();
      const cryptoSuite = new DefaultCryptoSuite();
      const options: ISigningOptions = {
        algorithm: <Algorithm>{name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256'},
        cryptoFactory: new CryptoFactory(keyStore, cryptoSuite)
      };

      const key = new RsaPrivateKey();
      key.e = 'AQAB';
      key.d = 'Eq5xpGnNCivDflJsRQBXHx1hdR1k6Ulwe2JZD50LpXyWPEAeP88vLNO97IjlA7_GQ5sLKMgvfTeXZx9SE-7YwVol2NXOoAJe46sui395IW_GO-pWJ1O0BkTGoVEn2bKVRUCgu-GjBVaYLU6f3l9kJfFNS3E0QbVdxzubSu3Mkqzjkn439X0M_V51gfpRLI9JYanrC4D4qAdGcopV_0ZHHzQlBjudU2QvXt4ehNYTCBr6XCLQUShb1juUO1ZdiYoFaFQT5Tw8bGUl_x_jTj3ccPDVZFD9pIuhLhBOneufuBiB4cS98l2SR_RQyGWSeWjnczT0QU91p1DhOVRuOopznQ';
      key.n = 'ofgWCuLjybRlzo0tZWJjNiuSfb4p4fAkd_wWJcyQoTbji9k0l8W26mPddxHmfHQp-Vaw-4qPCJrcS2mJPMEzP1Pt0Bm4d4QlL-yRT-SFd2lZS-pCgNMsD1W_YpRPEwOWvG6b32690r2jZ47soMZo9wGzjb_7OMg0LOL-bSf63kpaSHSXndS5z5rexMdbBYUsLA9e-KXBdQOS-UTo7WTBEMa2R2CapHg665xsmtdVMTBQY4uDZlxvb3qCo5ZwKh9kG4LT6_I5IhlJH7aGhyxXFvUK-DWNmoudF8NAco9_h9iaGNj8q2ethFkMLs91kzk2PAcDTW9gb54h4FRWyuXpoQ';
      key.p = '4BzEEOtIpmVdVEZNCqS7baC4crd0pqnRH_5IB3jw3bcxGn6QLvnEtfdUdiYrqBdss1l58BQ3KhooKeQTa9AB0Hw_Py5PJdTJNPY8cQn7ouZ2KKDcmnPGBY5t7yLc1QlQ5xHdwW1VhvKn-nXqhJTBgIPgtldC-KDV5z-y2XDwGUc';
      key.q = 'uQPEfgmVtjL0Uyyx88GZFF1fOunH3-7cepKmtH4pxhtCoHqpWmT8YAmZxaewHgHAjLYsp1ZSe7zFYHj7C6ul7TjeLQeZD_YwD66t62wDmpe_HlB-TnBA-njbglfIsRLtXlnDzQkv5dTltRJ11BKBBypeeF6689rjcJIDEz9RWdc';
      key.dp = 'BwKfV3Akq5_MFZDFZCnW-wzl-CCo83WoZvnLQwCTeDv8uzluRSnm71I3QCLdhrqE2e9YkxvuxdBfpT_PI7Yz-FOKnu1R6HsJeDCjn12Sk3vmAktV2zb34MCdy7cpdTh_YVr7tss2u6vneTwrA86rZtu5Mbr1C1XsmvkxHQAdYo0';
      key.dq = 'h_96-mK1R_7glhsum81dZxjTnYynPbZpHziZjeeHcXYsXaaMwkOlODsWa7I9xXDoRwbKgB719rrmI2oKr6N3Do9U0ajaHF-NKJnwgjMd2w9cjz3_-kyNlxAr2v4IKhGNpmM5iIgOS1VZnOZ68m6_pbLBSp3nssTdlqvd0tIiTHU';
      key.qi = 'IYd7DHOhrWvxkwPQsRM2tOgrjbcrfvtQJipd-DlcxyVuuM9sQLdgjVk2oy26F0EmpScGLq2MowX7fhd_QJQ3ydy5cY7YIBi87w93IKLEdfnbJtoOPLUW0ITrJReOgo1cq9SbsxYawBgfp_gh6A5603k2-ZQwVK0JKSHuLFkuQ3U';
      key.key_ops = [KeyOperation.Sign];

      await keyStore.save('key', key);
      const jwsToken = new JwsToken(options);
      const signature = await jwsToken.sign('key', payloadBuffer, ProtectionFormat.JwsCompactJson);
      expect(signature).toBeDefined();
      const encodedPayload = 'eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ';
      const encodedProtected = 'eyJhbGciOiJSUzI1NiJ9';
      const encodedSignature = 'cC4hiUPoj9Eetdgtv3hF80EGrhuB__dzERat0XF9g2VtQgr9PJbu3XOiZj5RZmh7AAuHIm4Bh-0Qc_lF5YKt_O8W2Fp5jujGbds9uJdbF9CUAr7t1dnZcAcQjbKBYNX4BAynRFdiuB--f_nZLgrnbyTyWzO75vRK5h6xBArLIARNPvkSjtQBMHlb1L07Qe7K0GarZRmB_eSN9383LcOLn6_dO--xi12jzDwusC-eOkHWEsqtFZESc6BfI7noOPqvhJ1phCnvWh6IeYI2w9QOYEUipUTI8np6LbgGY9Fs98rqVt5AXLIhWkWywlVmtVrBp0igcN_IoypGlUPQGe77Rw';
      expect(base64url.encode(signature.signatures[0].signature)).toEqual(encodedSignature);

      const compact = signature.serialize(ProtectionFormat.JwsCompactJson);
      expect(compact).toEqual(`${encodedProtected}.${encodedPayload}.${encodedSignature}`);

      const general = signature.serialize(ProtectionFormat.JwsGeneralJson);
      let parsed = JSON.parse(general);
      expect(parsed.payload).toEqual(encodedPayload);
      expect(parsed.signatures[0].signature).toEqual(encodedSignature);
      expect(parsed.signatures[0].protected).toEqual(encodedProtected);
      expect(parsed.signatures[0].header).toBeUndefined()

      const flat = signature.serialize(ProtectionFormat.JwsFlatJson);
      parsed = JSON.parse(flat);
      expect(parsed.payload).toEqual(encodedPayload);
      expect(parsed.signature).toEqual(encodedSignature);
      expect(parsed.protected).toEqual(encodedProtected);
      expect(parsed.header).toBeUndefined()
      });

});