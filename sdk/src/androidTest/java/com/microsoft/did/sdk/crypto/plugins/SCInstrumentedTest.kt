// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.stringToByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.generators.ECKeyPairGenerator
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECKeyGenerationParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.params.ParametersWithRandom
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.SecureRandom
import java.security.Security
import kotlin.experimental.and


@RunWith(AndroidJUnit4ClassRunner::class)
class SCInstrumentedTest {
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = Serializer()
        val keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, serializer)
    }

    @Test
    fun scTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val algorithm = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm,
                "KeyReference" to "keyReference"
            )
        )
        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        //generate key pair
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        val keyGen = ECKeyPairGenerator()
        val random = SecureRandom()
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val gParam = ECKeyGenerationParameters(ecDomainParameters, random)
        keyGen.init(gParam)
        val keyPair = keyGen.generateKeyPair()

        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", (keyPair.public as ECPublicKeyParameters).q.getEncoded(false))
        )

        val privateKey = CryptoKey(
            KeyType.Private,
            true,
            signAlgorithm,
            listOf(KeyUsage.Sign),
            Secp256k1Provider.Secp256k1Handle("", (keyPair.private as ECPrivateKeyParameters).d.toByteArray())
        )

        //Sign using SC
        val payload = stringToByteArray("testing")
        val md = SHA256Digest()
        md.reset()
        md.update(payload, 0, payload.size)
        val hashed = ByteArray(md.digestSize)
        md.doFinal(hashed, 0)

        val fsr_k = FixedSecureRandom()
        fsr_k.setBytes((keyPair.private as ECPrivateKeyParameters).d.toByteArray())

        val signingSigner = ECDSASigner()
        val privateKeyParams = ECPrivateKeyParameters((keyPair.private as ECPrivateKeyParameters).d, ecDomainParameters)
        val ecdsaprivrand = ParametersWithRandom(privateKeyParams, fsr_k)
        signingSigner.init(true, ecdsaprivrand)
        val components = signingSigner.generateSignature(hashed)
        val signature = encodeToDer(components[0].toByteArray(), components[1].toByteArray())

        val signingSigner2 = ECDSASigner()
        val privateKeyParams2 = ECPrivateKeyParameters(BigInteger((privateKey.handle as Secp256k1Provider.Secp256k1Handle).data), ecDomainParameters)
        assertThat(privateKeyParams).isEqualToComparingFieldByFieldRecursively(privateKeyParams2)
        signingSigner2.init(true, ecdsaprivrand)
        val components2 = signingSigner2.generateSignature(hashed)

        val sign = ellipticCurveSubtleCrypto.sign(alg, privateKey, hashed)

        //Verify using SC
        val verifySigner = ECDSASigner()
        val params = ECPublicKeyParameters((keyPair.public as ECPublicKeyParameters).q, ecDomainParameters)
        verifySigner.init(false, params)
        val verified = verifySigner.verifySignature(hashed, components2[0], components2[1])
        assertThat(verified).isTrue()

        val verify = ellipticCurveSubtleCrypto.verify(alg, publicKey, sign, hashed)
        assertThat(verify).isTrue()

        //Verify using Native lib
        var encsign = encodeToDer(sign.sliceArray(0..31), sign.sliceArray(32..63))
        val verifyAgain = ellipticCurveSubtleCrypto.nativeVerify(alg, publicKey, encsign, hashed)
        assertThat(verifyAgain).isTrue()
    }

    fun encodeToDer(r: ByteArray, s: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

    @Test
    fun verifyTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val algorithm = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm,
                "KeyReference" to "keyReference"
            )
        )
        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        val data = "eyJpc3MiOiJodHRwczovL3NlbGYtaXNzdWVkLm1lIiwic3ViIjoiRkxHT1Y3b2xtNmFkcEZwT0l3VTkxMzBqNlFxektTMmlWX0Y2cTBFckdTMCIsImF1ZCI6Imh0dHBzOi8vcG9ydGFibGVpZGVudGl0eWNhcmRzLmF6dXJlLWFwaS5uZXQvZGV2LXYxLjAvNTM2Mjc5ZjYtMTVjYy00NWYyLWJlMmQtNjFlMzUyYjUxZWVmL3BvcnRhYmxlSWRlbnRpdGllcy9jYXJkL2lzc3VlIiwiZGlkIjoiZGlkOmlvbjpFaUFiU1hiRjZhd1YwUGh3WllVcE5IM1VtdGg3b2lSS0FkOTNZUHh1d0ppNkNnPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbEJiRVl0TFdGWVNFOTNhVWRvYTJNeGMzQTVZbXRYY205RmJsWldabkJDVkRSb1YzWmxhRk5DWlZGcVFTSXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUkZXRUZRY1hSa1JIcFBOVkprWkUxd2JHTlFTM2RDUWxJMFIwOU9ORXgxYkZnM1NXWmtTRTFPZEhGQkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJFVllRVkJ4ZEdSRWVrODFVbVJrVFhCc1kxQkxkMEpDVWpSSFQwNDBUSFZzV0RkSlptUklUVTUwY1VFaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2lUbFp2WDNOcFoyNWZjREJSVlY4NE0zQmZNU0lzSW5SNWNHVWlPaUpGWTJSellWTmxZM0F5TlRack1WWmxjbWxtYVdOaGRHbHZia3RsZVRJd01Ua2lMQ0pxZDJzaU9uc2lhM1I1SWpvaVJVTWlMQ0pqY25ZaU9pSnpaV053TWpVMmF6RWlMQ0o0SWpvaVJWWklaMlJaZDBsSlVWZFpTVTVhYTBvMWFrWlpVMnQ2TVVOVlltYzFNM05PY1d0elJISmtkME15VlNJc0lua2lPaUpQVGtOaVNIVkJla3RtY204MmNqTk9abXBzWm1oT1gySjJSMXBTTjNwWmFFNWFkV0ZCU0RsdFNITlJJbjBzSW5CMWNuQnZjMlVpT2xzaVlYVjBhQ0lzSW1kbGJtVnlZV3dpWFgxZGZYMWRmUSIsInN1Yl9qd2siOnsia3R5IjoiRUMiLCJraWQiOiIjTlZvX3NpZ25fcDBRVV84M3BfMSIsInVzZSI6InNpZyIsImtleV9vcHMiOlsidmVyaWZ5Il0sImFsZyI6IkVTMjU2SyIsImNydiI6IlAtMjU2SyIsIngiOiJFVkhnZFl3SUlRV1lJTlprSjVqRllTa3oxQ1ViZzUzc05xa3NEcmR3QzJVIiwieSI6Ik9OQ2JIdUF6S2ZybzZyM05mamxmaE5fYnZHWlI3elloTlp1YUFIOW1Ic1EifSwiaWF0IjoxNTkyNDU5NjQ4LCJleHAiOjE1OTI2Mzk2MTYsImNvbnRyYWN0IjoiaHR0cHM6Ly9wb3J0YWJsZWlkZW50aXR5Y2FyZHMuYXp1cmUtYXBpLm5ldC9kZXYtdjEuMC81MzYyNzlmNi0xNWNjLTQ1ZjItYmUyZC02MWUzNTJiNTFlZWYvcG9ydGFibGVJZGVudGl0aWVzL2NvbnRyYWN0cy9CdXNpbmVzc0NhcmQiLCJqdGkiOiIzOGU4NjM4Yi05YzNmLTQzM2MtYTlmNS1hOWEwNGVjMGRiN2IiLCJhdHRlc3RhdGlvbnMiOnsic2VsZklzc3VlZCI6eyJmaXJzdF9uYW1lIjoibiIsImxhc3RfbmFtZSI6ImciLCJidXNpbmVzcyI6Im5nIn19fQ"
        val signature = "WEhNKVA6DG8GSoUbaj4wX9jRvZp3fGzQtV_5O0aYympVBFevrjZg2NtVUrGA5aT0vWzJMjDtwkoaO2U3AM2qJw"
        val x = "EVHgdYwIIQWYINZkJ5jFYSkz1CUbg53sNqksDrdwC2U"
        val y = "ONCbHuAzKfro6r3NfjlfhN_bvGZR7zYhNZuaAH9mHsQ"
        val d = Base64Url.decode(data)
        val s = Base64Url.decode(signature)
        val xb = Base64Url.decode(x)
        val yb = Base64Url.decode(y)
        val key = byteArrayOf(0x04)+xb+yb

        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", key)
        )

        val verified = ellipticCurveSubtleCrypto.verify(alg, publicKey, s, d)
        assertThat(verified).isTrue()
    }

    @Test
    fun bigintTest() {
        val r = BigInteger("10784286586387233585714474653313838895286657927039690371366624820669820862958")
//        val hexr = Hex.decode(r.toString())
        val rb = r.toByteArray()
//        assertThat(hexr).isEqualTo(rb)

        val rsSignature2 = "51C57E68B628B11E98EBD0619F1AAA82F1A362832A0DDDD9DE3FF5CD709CC9A2C5D1AE439DE2E9256C76CFABB74E93493794D24756607C228668184FDFBB08E1"
        val rsarr = Hex.decode(rsSignature2)
    }

    @Test
    fun testingDerFormat2() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val hash1 = "85EB4467104FBD9883BF4075EC79DEFDC6EC260B2898D4B4D195443C463B0ED3"
        val hash2 = "8AD4D31D84A0ACDDD96A49D2B730C8F972D1149A64C58B09077B80A787926DCB"
        val hash3 = "2E803D3ED613675AA43A83B36B55C396F58A662055949F597B7E3E13A41A2DB0"
        val hash4 = "77534F95CF5F6C94AEE6F0C9CBBD2B2B5BC3BA8A69AE797E4F6DE137398A999B"
        val hash5 = "FDB30FDEF8E7C4CE7A427A60F476BF4E7CEF626454806D7B8382E04229CB3058"
        val hash6 = "19F1805F6D170A9DB2DDA7F84F9671B48FC12164C2B57DA7DE7A6620D803A93B"
        val hash7 = "7666C65BBD40D8DE9193B5B1094E128859BAA2A2D5FE0933519E7444F16666D2"
        val hash8 = "D475FFEEE9B71A23DDFA418802A6538FE8331F37E0C2EC3598341061F975E2CB"
        val hash9 = "1420707FB0CFA70D32474FE5139D6DCAD85403F623B909F8D19CD519F1CE0FA3"
        val hash10 = "09F4A4089D832CE76E499B93C144E33905F75CACF98BD004535D9B90DC620FA5"
        val hash11 = "CACC5579204D1647223DDD69DB8F4441F09339AB0DF5F0601C2DE103E544A7F8"
        val hash12 = "C5073BAB4F0DB37955EA6F0F3D2C7D39F7DA044909FAC773E12EABFF7A0A82E0"
        val hash13 = "1737980EE97837ABF094005586518BD8C6009DE7F100F1F8EDC7B6ABF4DA9FAA"
        val hash14 = "4DFA5EF7F51AEFDACFA9395556B86ABF9DBFFE0EE2C10E075550A2891FFA5948"
        val hash15 = "E9B8307E0387A99FBB6BDE55E5FCB58747CCAEA9C427E51EFE54C02A299EF280"
        val hash16 = "1D424341A0519AD8FEA20FADF4ED55ED17A2D2249E63AD9D86FABBAB2BA7E402"
        val hash = hash1
        val hashByteArray = Hex.decode(hash)
        val publicKey = EllipticCurvePublicKey(
            JsonWebKey(
                kty = com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value,
                x = "r_1voElsuJnWrc7MLzqKeIQ2ZrlXP3UDfOYclKMvJWg",
                y = "-uYArMDYDpxaQ8_z9s1kGzToHPHvxORah_V_rhsLoEU"
            )
        )
        val cryptoKey = ellipticCurveSubtleCrypto.importKey(
            KeyFormat.Jwk,
            publicKey.toJWK(),
            Algorithm(W3cCryptoApiConstants.EcDsa.value),
            false,
            listOf(KeyUsage.Verify)
        )
        val keyData = (cryptoKey.handle as Secp256k1Provider.Secp256k1Handle).data
        val derSignature1 = "304402206C35A6C0F0BE1858DA4275DD60E69EA174E20B3D6E66FD9E4A9C385BEE7F1DD102202054DED0D1E5DED54F763C3B468333EE2E1116E8AE22A51A0FF521A0EBBE3C62"
        val derSignature2 = "3045022051C57E68B628B11E98EBD0619F1AAA82F1A362832A0DDDD9DE3FF5CD709CC9A2022100C5D1AE439DE2E9256C76CFABB74E93493794D24756607C228668184FDFBB08E1"
        val derSignature3 = "3046022100ACFFCA0FD8FB2905B9B61358E4EA064D353E49FA799CE2163F4CC2763A7E553B022100D218A9A321FB8DE9C2240FDEED4CF1EDBF5D35C47D18195CBD7769E76790B22E"
        val derSignature4 = "3045022100EEA4D9BF154B1F5E060019520F7532F6E81AA799609EE4DBEAC3DD9E974C0F5602205490BD9AD3368DDFA16F3D2B14E0ECBECDDDC935A4B59488568460AC2465E8A0"
        val derSignature5 = "304302201A5F03B216A8ABDBB25CD8596C71DD61F82949CA7E0C8F5AF15297A6BBA2D978021F2E415CA5198E7750D16ED4D312A9782341A89150134C0BD96DBE869C6F739E"
        val derSignature6 = "3045022000B1647CEE8B1E736D9198616801BC59FE920301C49DEC308803B986EE2DCA46022100F129FFB0E6EA8462F7D958B1D214239CDD5F51E754832CBC89A96BE84B5A98C1"
        val derSignature7 = "304402210089E44707362D657E3E1C52128783F2EB473159D279638C24720331683E475D36021F77FF30B48E601D02DEF4043350E2B2EDDA9E3B403EEFE38A9B8FDC3B44E088"
        val derSignature8 = "3045022100A00104D21C8D8F0CD55ABA7853B6CB8261F52F6BEB440FB194E7210FDDABECD602200095C5264B70957D358AE6003E4DA83123F104EE71D24B543DD4332CDF77D70E"
        val derSignature9 = "3044021F1E4BC1121CCA5C745BD94AD4C9038CC6BCB7B429AA8F87D07F79EEA12DC14D022100FF7355548A2C668414CB26746928F740CD97CC55CCF409EF38C9AEB574BB2151"
        val derSignature10 = "3043021F4F0C7FD008D741A126BFEC4354972E375EECA43CA6DB93C4C4469C0C8B854902204F77387CDC3330515E9B0B44007CC9C0DBC62DDDF478BC89246ADADA25594E84"
        val derSignature11 = "3044022047A3D8BF86F7F212B14486E7FF5E66653CD1C12AA047604C02A8582D32C54C86022000AAD92B3FBF50C79245D6F2FA41545A7B10C5254B66E1D89763A0CA147D1B94"
        val derSignature12 = "3044022000E31AE33E8D15EA1E64C4E411B87F52A06AD9FA67E0968FF43F585F63D12DF2022011590D3F4BF83C63B28C6EA6425B0E83E3C87EAEA15F2014E5EFC6982254E9DC"
        val derSignature13 = "3043022000D368ADF65B8E04BA7FC62933DE1E77A6F915D43562F7651281A927478BCA7A021F68B37E4A7E1992ACE3F723B27C5943CE3D45DCDA368AF191F797539B358F94"
        val derSignature14 = "3043021F5F7F8604A16C2D59EC0875D3DF621D8B8FB5CAA83538183EB9BE65378ECB5C022000D6DA9084654B420EBB62FAA114DFB33B143E93ABBFA30963B4B630AFA0451B"
        val derSignature15 = "3042021F7F9625E7D0B625004848DFBBA23F05BF15951CF881D204F8A89192A980813A021F6E4DABFED97160C92C16F52378169AD98963A2DBF2F5DACB68C365D3DD4A8B"
        val derSignature16 = "3044022000821962339420B3F73F4DA7E46AFD7654DE0AAEDF23C702AB4FF5A3165E3527022000D44659E935CD95F5734A8D5DC79FC5EECD6ADA1622D843102878E1C7D9CE04"
        val derSignature = derSignature1
        val derSignatureByteArray = Hex.decode(derSignature)
//        val matchedDER = ellipticCurveSubtleCrypto.verify(alg, cryptoKey, derSignatureByteArray, keyData)
//        assertThat(matchedDER).isTrue()
        val rsSignature1 = "6C35A6C0F0BE1858DA4275DD60E69EA174E20B3D6E66FD9E4A9C385BEE7F1DD12054DED0D1E5DED54F763C3B468333EE2E1116E8AE22A51A0FF521A0EBBE3C62"
        val rsSignature2 = "51C57E68B628B11E98EBD0619F1AAA82F1A362832A0DDDD9DE3FF5CD709CC9A2C5D1AE439DE2E9256C76CFABB74E93493794D24756607C228668184FDFBB08E1"
        val rsSignature3 = "ACFFCA0FD8FB2905B9B61358E4EA064D353E49FA799CE2163F4CC2763A7E553BD218A9A321FB8DE9C2240FDEED4CF1EDBF5D35C47D18195CBD7769E76790B22E"
        val rsSignature4 = "EEA4D9BF154B1F5E060019520F7532F6E81AA799609EE4DBEAC3DD9E974C0F565490BD9AD3368DDFA16F3D2B14E0ECBECDDDC935A4B59488568460AC2465E8A0"
        val rsSignature5 = "1A5F03B216A8ABDBB25CD8596C71DD61F82949CA7E0C8F5AF15297A6BBA2D978002E415CA5198E7750D16ED4D312A9782341A89150134C0BD96DBE869C6F739E"
        val rsSignature6 = "00B1647CEE8B1E736D9198616801BC59FE920301C49DEC308803B986EE2DCA46F129FFB0E6EA8462F7D958B1D214239CDD5F51E754832CBC89A96BE84B5A98C1"
        val rsSignature7 = "89E44707362D657E3E1C52128783F2EB473159D279638C24720331683E475D360077FF30B48E601D02DEF4043350E2B2EDDA9E3B403EEFE38A9B8FDC3B44E088"
        val rsSignature8 = "A00104D21C8D8F0CD55ABA7853B6CB8261F52F6BEB440FB194E7210FDDABECD60095C5264B70957D358AE6003E4DA83123F104EE71D24B543DD4332CDF77D70E"
        val rsSignature9 = "001E4BC1121CCA5C745BD94AD4C9038CC6BCB7B429AA8F87D07F79EEA12DC14DFF7355548A2C668414CB26746928F740CD97CC55CCF409EF38C9AEB574BB2151"
        val rsSignature10 = "004F0C7FD008D741A126BFEC4354972E375EECA43CA6DB93C4C4469C0C8B85494F77387CDC3330515E9B0B44007CC9C0DBC62DDDF478BC89246ADADA25594E84"
        val rsSignature11 = "47A3D8BF86F7F212B14486E7FF5E66653CD1C12AA047604C02A8582D32C54C8600AAD92B3FBF50C79245D6F2FA41545A7B10C5254B66E1D89763A0CA147D1B94"
        val rsSignature12 = "00E31AE33E8D15EA1E64C4E411B87F52A06AD9FA67E0968FF43F585F63D12DF211590D3F4BF83C63B28C6EA6425B0E83E3C87EAEA15F2014E5EFC6982254E9DC"
        val rsSignature13 = "00D368ADF65B8E04BA7FC62933DE1E77A6F915D43562F7651281A927478BCA7A0068B37E4A7E1992ACE3F723B27C5943CE3D45DCDA368AF191F797539B358F94"
        val rsSignature14 = "005F7F8604A16C2D59EC0875D3DF621D8B8FB5CAA83538183EB9BE65378ECB5C00D6DA9084654B420EBB62FAA114DFB33B143E93ABBFA30963B4B630AFA0451B"
        val rsSignature15 = "007F9625E7D0B625004848DFBBA23F05BF15951CF881D204F8A89192A980813A006E4DABFED97160C92C16F52378169AD98963A2DBF2F5DACB68C365D3DD4A8B"
        val rsSignature16 = "00821962339420B3F73F4DA7E46AFD7654DE0AAEDF23C702AB4FF5A3165E352700D44659E935CD95F5734A8D5DC79FC5EECD6ADA1622D843102878E1C7D9CE04"
        val rsSignature = rsSignature1
        val rsSignatureByteArray = Hex.decode(rsSignature)
        val convertedDerSignatureByteArray = convertRSToDER(rsSignatureByteArray)
//        val matchedRSConverted = ellipticCurveSubtleCrypto.verify(alg, cryptoKey, derSignatureByteArray, keyData)
        val matchedRSConverted = ellipticCurveSubtleCrypto.verify(alg, cryptoKey, rsSignatureByteArray, hashByteArray)
        assertThat(matchedRSConverted).isTrue()
    }

    private fun convertRSToDER(rsSignatureByteArray: ByteArray): ByteArray {
        var additionalBytes = 4
        var isRNegative = false
        var isSNegative = false
        val rByteArray = rsSignatureByteArray.copyOfRange(0, 32)
        var rStart = 0
        for(element in rByteArray) {
            if(element.toInt() == 0)
                rStart++
            else
                break
        }
        val sByteArray = rsSignatureByteArray.copyOfRange(32, rsSignatureByteArray.size)
        var sStart = 32
        for(element in sByteArray) {
            if(element.toInt() == 0)
                sStart++
            else
                break
        }
        var rSize = rByteArray.size - rStart
        var sSize = sByteArray.size - sStart + 32
        if ((rsSignatureByteArray[rStart] and 0x80.toByte()).compareTo(0x80.toByte()) == 0) {
//            additionalBytes++
            isRNegative = true
            rSize++
        }
        if ((rsSignatureByteArray[sStart] and 0x80.toByte()).compareTo(0x80.toByte()) == 0) {
//            additionalBytes++
            isSNegative = true
            sSize++
        }
        val totalSize = rSize + sSize + additionalBytes
        var derSignatureByteArray: ByteArray =
            byteArrayOf(48, totalSize.toByte(), 2, rSize.toByte())
        if(isRNegative)
            derSignatureByteArray += 0
        derSignatureByteArray += rsSignatureByteArray.copyOfRange(rStart, 32)
        derSignatureByteArray += byteArrayOf(
            2,
            sSize.toByte()
        )
        if(isSNegative)
            derSignatureByteArray += 0
        derSignatureByteArray += rsSignatureByteArray.copyOfRange(sStart, rsSignatureByteArray.size)
        return derSignatureByteArray
    }


}

class FixedSecureRandom : SecureRandom() {
    private var nextBytesIndex = 0
    private var nextBytesValues: ByteArray? = null
    fun setBytes(values: ByteArray?) {
        nextBytesValues = values
    }

    override fun nextBytes(b: ByteArray) {
        if (nextBytesValues == null) {
            super.nextBytes(b)
        } else if (nextBytesValues!!.size == 0) {
            super.nextBytes(b)
        } else {
            for (i in b.indices) {
                b[i] = nextBytesValues!![nextBytesIndex]
                nextBytesIndex = (nextBytesIndex + 1) % nextBytesValues!!.size
            }
        }
    }

    companion object {
        private const val debug = false
        private const val serialVersionUID = 1L
    }
}