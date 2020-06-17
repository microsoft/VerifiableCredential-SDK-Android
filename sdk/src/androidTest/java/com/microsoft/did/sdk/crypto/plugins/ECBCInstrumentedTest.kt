// Copyright (c) Microsoft Corporation. All rights reserved
package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.io.BaseEncoding
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.util.Base64
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.stringToByteArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.asn1.DLSequence
import org.spongycastle.asn1.sec.SECNamedCurves
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.crypto.signers.HMacDSAKCalculator
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.ECPointUtil
import org.spongycastle.jce.interfaces.ECPublicKey
import org.spongycastle.jce.spec.ECNamedCurveSpec
import org.spongycastle.util.encoders.Hex
import org.spongycastle.util.test.FixedSecureRandom
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.EllipticCurve

@RunWith(AndroidJUnit4ClassRunner::class)
class ECBCInstrumentedTest {
    private val androidSubtle: AndroidSubtle
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private lateinit var cryptoKeyPair: CryptoKeyPair

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val serializer = Serializer()
        val keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, serializer)
    }

    @Test
    fun generateKeyPairTest() {
        val keyReference = "KeyReference1"
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
        val private = cryptoKeyPair.privateKey
        val public = cryptoKeyPair.publicKey

        val privateJwk = ellipticCurveSubtleCrypto.exportKeyJwk(private)
        val publicJwk = ellipticCurveSubtleCrypto.exportKeyJwk(public)

        assertThat(privateJwk.x).isEqualTo(publicJwk.x)
        assertThat(privateJwk.y).isEqualTo(publicJwk.y)

        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val privateKey = ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, privateJwk, alg, true, listOf(KeyUsage.Sign))
        val publicKey = ellipticCurveSubtleCrypto.importKey(KeyFormat.Jwk, publicJwk, alg, true, listOf(KeyUsage.Verify))

        assertThat(private.handle).isEqualToComparingFieldByFieldRecursively(privateKey.handle)
        assertThat(public.handle).isEqualToComparingFieldByFieldRecursively(publicKey.handle)

        val generatedPrivateKey = generatePrivateKeyFromCryptoKey(privateKey)
        assertThat((generatedPrivateKey as BCECPrivateKey).d.toByteArray()).isEqualTo((private.handle as Secp256k1Provider.Secp256k1Handle).data)

        val generatedPublicKey = generatePublicKeyFromCryptoKey(publicKey)
        assertThat((generatedPublicKey as BCECPublicKey).q.xCoord.encoded).isEqualTo(
            (public.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(
                0..31
            )
        )
        assertThat(generatedPublicKey.q.yCoord.encoded).isEqualTo((public.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(32..63))

        val testData = "test message"
        val signature = ellipticCurveSubtleCrypto.sign(alg, privateKey, stringToByteArray(testData))

        val verified = ellipticCurveSubtleCrypto.verify(alg, publicKey, signature, stringToByteArray(testData))
        assertThat(verified).isTrue()
    }

    private fun generatePrivateKeyFromByteArray(key: ByteArray): PrivateKey {
        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateSpec = java.security.spec.ECPrivateKeySpec(BigInteger(key), curveSpec)
        return keyFactory.generatePrivate(privateSpec)
    }

    private fun generatePrivateKeyFromCryptoKey(key: CryptoKey): PrivateKey {
        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateSpec =
            java.security.spec.ECPrivateKeySpec(BigInteger((key.handle as Secp256k1Provider.Secp256k1Handle).data), curveSpec)
        return keyFactory.generatePrivate(privateSpec)
    }

    private fun generatePublicKeyFromCryptoKey(key: CryptoKey): PublicKey {
/*        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: java.security.spec.ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val publicKeyJwk = ellipticCurveSubtleCrypto.exportKeyJwk(key)
        val publicKeySpec =
            java.security.spec.ECPublicKeySpec(
                ECPoint(
                    BigInteger((key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(0..31)),
                    BigInteger((key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(32..63))
                ),
                curveSpec
            )
        return keyFactory.generatePublic(publicKeySpec)*/

        val params = ECNamedCurveTable.getParameterSpec("secp256k1")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val curve = params.curve
        val ellipticCurve: EllipticCurve = EC5Util.convertCurve(curve, params.seed)
        val x = (key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(0..31)
        val y = (key.handle as Secp256k1Provider.Secp256k1Handle).data.sliceArray(32..63)
        val encoded = byteArrayOf(0x04) + x + y
        val point: ECPoint = ECPointUtil.decodePoint(ellipticCurve, encoded)
        val params2: ECParameterSpec = EC5Util.convertSpec(ellipticCurve, params)
        val keySpec = ECPublicKeySpec(point, params2)
        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    @Test
    fun nativeSignAndSCVerifyTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        var verified = false
        var testData = "this is a random test message for testing signing and verifying"
        for (i in 0..10) {
            val keyReference = "KeyReference$i"
            cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
                EcKeyGenParams(
                    namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                    additionalParams = mapOf(
                        "hash" to Sha.SHA256.algorithm,
                        "KeyReference" to keyReference
                    )
                ), true, listOf(KeyUsage.Sign)
            )
            val private = cryptoKeyPair.privateKey
            val public = cryptoKeyPair.publicKey
            testData += i
            val signature = ellipticCurveSubtleCrypto.onNativeSign(alg, private, stringToByteArray(testData))

            verified = ellipticCurveSubtleCrypto.verify(alg, public, signature, stringToByteArray(testData))
            if (verified)
                break
        }
        assertThat(verified).isTrue()
    }

    @Test
    fun scSignAndNativeVerifyTest() {
        val alg = EcdsaParams(
            hash = Algorithm(
                name = W3cCryptoApiConstants.Sha256.value
            ),
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        var testData = "this is a random test message for testing signing and verifying"
//        var testData = """eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQTJoSVIzZEhtQ1U5U1ZyckQyWDFqZU04NHo5RjBWRlVteU9XTElnWGs4WWc_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFVtVXlaSEJvYzBWM1duYzNkRVk1YzNGTmMxcHNVbnBLUVdvelEzWTBYMHBsV0ZkdlYxaDVjbGxDUVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSemIwWnlaa1prWlMxZlJqUjBSR1ExUmxCSmEzSlVRVXRDTjNOM1RHZEJOMkZVZW5KWU1taElSMGxuSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkhOdlJuSm1SbVJsTFY5R05IUkVaRFZHVUVscmNsUkJTMEkzYzNkTVowRTNZVlI2Y2xneWFFaEhTV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWNpMXpYM05wWjI1ZmNXNTNiVGxJTm5OZk1TSXNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl3TVRraUxDSnFkMnNpT25zaWEzUjVJam9pUlVNaUxDSmpjbllpT2lKelpXTndNalUyYXpFaUxDSjRJam9pTTI1elYwdGpMWEZCTm10WU5tUXlSV2xmYVVKcVRVMVlXVEJWY1ZOblFYQjZlWGxOU25Sb1JWaEhaeUlzSW5raU9pSlRORXRQTTBsclZrUXpNMjFrWW04d2NGWmFVRVY1YzJzMlkzaHJXbmxKUTFsVFZWSkJZMk5TZFZGckluMHNJbkIxY25CdmMyVWlPbHNpWVhWMGFDSXNJbWRsYm1WeVlXd2lYWDFkZlgxZGZRI3Itc19zaWduX3Fud205SDZzXzEifQ.eyJpc3MiOiJodHRwczovL3NlbGYtaXNzdWVkLm1lIiwic3ViIjoiWEt5WkJvbFUzaXFuUHg5aEpSRUZGQTdOSUJ4SVJLbXlqRXlZSHpuNFhJTSIsImF1ZCI6Imh0dHBzOi8vcG9ydGFibGVpZGVudGl0eWNhcmRzLmF6dXJlLWFwaS5uZXQvZGV2LXYxLjAvNTM2Mjc5ZjYtMTVjYy00NWYyLWJlMmQtNjFlMzUyYjUxZWVmL3BvcnRhYmxlSWRlbnRpdGllcy9jYXJkL2lzc3VlIiwiZGlkIjoiZGlkOmlvbjpFaUEyaElSM2RIbUNVOVNWcnJEMlgxamVNODR6OUYwVkZVbXlPV0xJZ1hrOFlnPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbERVbVV5WkhCb2MwVjNXbmMzZEVZNWMzRk5jMXBzVW5wS1FXb3pRM1kwWDBwbFdGZHZWMWg1Y2xsQ1FTSXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnpiMFp5Wmtaa1pTMWZSalIwUkdRMVJsQkphM0pVUVV0Q04zTjNUR2RCTjJGVWVuSllNbWhJUjBsbkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJITnZSbkptUm1SbExWOUdOSFJFWkRWR1VFbHJjbFJCUzBJM2MzZE1aMEUzWVZSNmNsZ3lhRWhIU1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljaTF6WDNOcFoyNWZjVzUzYlRsSU5uTmZNU0lzSW5SNWNHVWlPaUpGWTJSellWTmxZM0F5TlRack1WWmxjbWxtYVdOaGRHbHZia3RsZVRJd01Ua2lMQ0pxZDJzaU9uc2lhM1I1SWpvaVJVTWlMQ0pqY25ZaU9pSnpaV053TWpVMmF6RWlMQ0o0SWpvaU0yNXpWMHRqTFhGQk5tdFlObVF5UldsZmFVSnFUVTFZV1RCVmNWTm5RWEI2ZVhsTlNuUm9SVmhIWnlJc0lua2lPaUpUTkV0UE0wbHJWa1F6TTIxa1ltOHdjRlphVUVWNWMyczJZM2hyV25sSlExbFRWVkpCWTJOU2RWRnJJbjBzSW5CMWNuQnZjMlVpT2xzaVlYVjBhQ0lzSW1kbGJtVnlZV3dpWFgxZGZYMWRmUSIsInN1Yl9qd2siOnsia3R5IjoiRUMiLCJraWQiOiIjci1zX3NpZ25fcW53bTlINnNfMSIsInVzZSI6InNpZyIsImtleV9vcHMiOlsidmVyaWZ5Il0sImFsZyI6IkVTMjU2SyIsImNydiI6IlAtMjU2SyIsIngiOiIzbnNXS2MtcUE2a1g2ZDJFaV9pQmpNTVhZMFVxU2dBcHp5eU1KdGhFWEdnIiwieSI6IlM0S08zSWtWRDMzbWRibzBwVlpQRXlzazZjeGtaeUlDWVNVUkFjY1J1UWsifSwiaWF0IjoxNTkyMjk1Mjk2LCJleHAiOjE1OTI0NzUyNjQsImNvbnRyYWN0IjoiaHR0cHM6Ly9wb3J0YWJsZWlkZW50aXR5Y2FyZHMuYXp1cmUtYXBpLm5ldC9kZXYtdjEuMC81MzYyNzlmNi0xNWNjLTQ1ZjItYmUyZC02MWUzNTJiNTFlZWYvcG9ydGFibGVJZGVudGl0aWVzL2NvbnRyYWN0cy9JZGVudGl0eUNhcmQiLCJqdGkiOiI1MTFlMDM4ZS04YTJkLTQyZDEtYmVlYS1hMGY4OTIwMzVlZjQiLCJhdHRlc3RhdGlvbnMiOnsiaWRUb2tlbnMiOnsiaHR0cHM6Ly9jdXN0b21lcnNnbG9iYWxseS5iMmNsb2dpbi5jb20vY3VzdG9tZXJzZ2xvYmFsbHkub25taWNyb3NvZnQuY29tL3YyLjAvLndlbGwta25vd24vb3BlbmlkLWNvbmZpZ3VyYXRpb24_cD1CMkNfMV9kZWZhdWx0IjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKU1V6STFOaUlzSW10cFpDSTZJbGcxWlZock5IaDViMnBPUm5WdE1XdHNNbGwwZGpoa2JFNVFOQzFqTlRka1R6WlJSMVJXUW5kaFRtc2lmUS5leUpsZUhBaU9qRTFPVEl5T1RnNE16Z3NJbTVpWmlJNk1UVTVNakk1TlRJek9Dd2lkbVZ5SWpvaU1TNHdJaXdpYVhOeklqb2lhSFIwY0hNNkx5OWpkWE4wYjIxbGNuTm5iRzlpWVd4c2VTNWlNbU5zYjJkcGJpNWpiMjB2Tm1JMFltTXdZMkl0WWprNU55MDBNRFk1TFRsbE5qUXRaamsxTXpnMk5URTJNV0pqTDNZeUxqQXZJaXdpYzNWaUlqb2lOakF3Tm1VNFltRXRaak5oTWkwME9UbGpMVGcwWlRrdE1EVXdOak0wTVRjME9EazNJaXdpWVhWa0lqb2lOR0ZsTkRSbE5XWXRNMlZqTVMwME9XTXhMVGszWVdJdE5ERXdZV0poWVRjMU1HRmpJaXdpYm05dVkyVWlPaUl5TVRReE5UYzNOems0SWl3aWFXRjBJam94TlRreU1qazFNak00TENKaGRYUm9YM1JwYldVaU9qRTFPVEl5T1RVeU16Z3NJbVY0ZEdWdWMybHZibDlEVDFaSlJGOHhPVjlKYlcxMWJtVWlPblJ5ZFdVc0ltVjRkR1Z1YzJsdmJsOUJaMlVpT2lJek1pSXNJbU5wZEhraU9pSlRaV0YwZEd4bElpd2lZMjkxYm5SeWVTSTZJbFZ1YVhSbFpDQlRkR0YwWlhNaUxDSnVZVzFsSWpvaVFXeHBZMlVnVTIxcGRHZ2lMQ0puYVhabGJsOXVZVzFsSWpvaVFXeHBZMlVpTENKbGVIUmxibk5wYjI1ZlIzSmhaSFZoZEdsdmJsOVpaV0Z5SWpveU1ERXlMQ0pxYjJKVWFYUnNaU0k2SWtSdlkzUnZjaUlzSW1WNGRHVnVjMmx2Ymw5TllXcHZjaUk2SWtKcGIyeHZaM2tpTENKbGVIUmxibk5wYjI1ZlRVTkJWRjlUWTI5eVpTSTZJalV5TUNJc0luQnZjM1JoYkVOdlpHVWlPaUk1T0RFd09TSXNJbk4wWVhSbElqb2lWMEVpTENKemRISmxaWFJCWkdSeVpYTnpJam9pTVRJek5DQlVaWEp5ZVNCQmRtVWdUaUlzSW1aaGJXbHNlVjl1WVcxbElqb2lVMjFwZEdnaUxDSjBabkFpT2lKQ01rTmZNVjlrWldaaGRXeDBJbjAuVTVpUTJCbTk2Ym1yUUVOVW1ET2J4NmlGZ0YyYmNoeHdZSzlOQVJVV2ZXVnVxenNHY255UXhHd2g2UGJMbUxGdlFUVi1Fa1ZuQmQxdzJKQ0Z4a0FSSkdnUjVma0VYOXFuV05Kb3FycTY2QmRsUXlfVkluV1lLeDhLOS1FUXNORXdMQ255YWxEY3hQWWdSWTdkR2NHMjBKVElMcmdoRDlQdmx6cFFOQmpUeXA3M0lYZzlCeUJGZmhtR1cxTXN1VlF1bUFvaDBfcHA4MXJIMkNYbVE1YTBRVUlWUjkzeVVDOWZmTVd5LXhRb2JmZkFDVnJZYmpWTUZpVjBFSzdDMXljQlVjOFhTMElFaHkwejM0TzBNc2ktSmJoUWdUZTlkd1NjNHpQTkhzRTlSVENmSkIwNUU0NzRwREhCZ21LMlZrVlhQRTFtYkdFajBMVmpPYWw0ajd4SkxBIn19fQ"""
        val keyReference = "KeyReference1"
        cryptoKeyPair = ellipticCurveSubtleCrypto.generateKeyPair(
            EcKeyGenParams(
                namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                additionalParams = mapOf(
                    "hash" to Sha.SHA256.algorithm,
                    "KeyReference" to keyReference
                )
            ), true, listOf(KeyUsage.Sign)
        )
        val private = cryptoKeyPair.privateKey
        val public = cryptoKeyPair.publicKey
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val dat = digest.digest(stringToByteArray("testing"))
        val data =
            BaseEncoding.base16().lowerCase().decode("CF80CD8AED482D5D1527D7DC72FCEFF84E6326592848447D2DC0B0E87DFC9A90".toLowerCase())
        val sec = BaseEncoding.base16().lowerCase().decode("67E56582298859DDAE725F972992A07C6C4FB9F62A8FFF58CE3CA926A1063530".toLowerCase())
        val pub = BaseEncoding.base16().lowerCase()
            .decode("040A629506E1B65CD9D2E0BA9C75DF9C4FED0DB16DC9625ED14397F0AFC836FAE595DC53F8B0EFE61E703075BD9B143BAC75EC0E19F82A2208CAEB32BE53414C40".toLowerCase())
        val algorithm = EcKeyGenParams(
            namedCurve = W3cCryptoApiConstants.Secp256k1.value,
            additionalParams = mapOf(
                "hash" to Sha.SHA256.algorithm,
                "KeyReference" to keyReference
            )
        )
        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", pub/*.sliceArray(1..64)*/)
        )
        var signature = sign(sec, data)
        val actual =
            "30440220182A108E1448DC8F1FB467D06A0F3BB8EA0533584CB954EF8DA112F1D60E39A202201C66F36DA211C087F3AF88B50EDF4F9BDAA6CF5FD6817E74DCA34DB12390C6E9"
        val actualSign = BaseEncoding.base16().lowerCase().decode(actual.toLowerCase())
//        val rs = decodeFromDER(signature)
//        val canonicalized = canonicalize(rs.first, rs.second)
//        signature = encodeToDer(canonicalized.first.toByteArray(), canonicalized.second.toByteArray())
/*        val v = ellipticCurveSubtleCrypto.verify(alg, publicKey, actualSign, data)
        assertThat(v).isTrue()
        assertEquals( signature,actualSign )*/

        val verified = ellipticCurveSubtleCrypto.nativeVerify(alg, publicKey, actualSign, data)
        assertThat(verified).isTrue()
    }

    @Test
    fun verifyTest() {
        val sigstring = "MEQCIGhaF_7-R9g6RqdypGCDG9S_84tYBjx7UsaaWBA4H5Q-AiAWnJYQ-k7cLMfIQ8PjOpqrlIZRN9X-85olVx7ukkiIOQ"
        val signature = Base64Url.decode(sigstring)
//        verified = ellipticCurveSubtleCrypto.nativeVerify(alg, public, signature, stringToByteArray(testData))
    }

    fun sign(privateKey: ByteArray, data: ByteArray): Pair<BigInteger, BigInteger> {
//        val privateKey = (key.handle as Secp256k1Provider.Secp256k1Handle).data
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
        val privateKeyParams = ECPrivateKeyParameters(BigInteger(privateKey), ecDomainParameters)
        signer.init(true, privateKeyParams)
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashedData = digest.digest(data)
        val components = signer.generateSignature(hashedData)
        return Pair(components[0], components[1])
//        return encodeToDer(components[0].toByteArray(), components[1].toByteArray())
/*        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(generatePrivateKeyFromByteArray(privateKey))
        signature.update(data)
        return signature.sign()*/
    }

    fun encodeToDer(r: ByteArray, s: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

    fun canonicalize(r: BigInteger, s: BigInteger): Pair<BigInteger, BigInteger> {
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        return if (s > ecParams.n.shiftRight(1)) {
            val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
            val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
            Pair(r, ecDomainParameters.n.subtract(s))
        } else
            Pair(r, s)
    }

    fun decodeFromDER(bytes: ByteArray): Pair<BigInteger, BigInteger> {
        val decoder = ASN1InputStream(bytes)
        try {
            val seq = decoder.readObject() as DLSequence
            val r = seq.getObjectAt(0) as ASN1Integer
            val s = seq.getObjectAt(1) as ASN1Integer
            return Pair(r.positiveValue, s.positiveValue)
        } catch (e: IOException) {
            throw RuntimeException(e);
        } finally {
            if (decoder != null)
                try {
                    decoder.close()
                } catch (x: IOException) {
                }
        }
    }

    @Test
    fun testECDSASecP256k1sha256()
    {
        val p = SECNamedCurves.getByName ("secp256k1");
        val params = ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());
        val priKey = ECPrivateKeyParameters(
            BigInteger ("ebb2c082fd7727890a28ac82f6bdf97bad8de9f5d7c9028692de1a255cad3e0f", 16
        ), // d
        params);
        val k = FixedSecureRandom(Hex.decode("49a0d7b786ec9cde0d0721d72804befd06571c974b191efb42ecf322ba9ddd9a"));

        val M = Hex.decode ("4b688df40bcedbe641ddb16ff0a1842d9c67ea1c3bf63f3e0471baa664531d1a");

        val dsa = ECDSASigner()

        dsa.init(true, priKey/*ParametersWithRandom (priKey, k)*/);

        val sig = dsa.generateSignature (M);

        val r = BigInteger("241097efbf8b63bf145c8961dbdf10c310efbb3b2676bbc0f8b08505c9e2f795", 16);
        val s = BigInteger("21006b7838609339e8b415a7f9acb1b661828131aef1ecbc7955dfb01f3ca0e", 16);

        if (!r.equals(sig[0])) {
            println("r is wrong")
        }

        if (!s.equals(sig[1])) {
            println("s is wrong")
        }

        // Verify the signature
        val pubKey = ECPublicKeyParameters(
            params.getCurve().decodePoint(Hex.decode("04779dd197a5df977ed2cf6cb31d82d43328b790dc6b3b7d4437a427bd5847dfcde94b724a555b6d017bb7607c3e3281daf5b1699d6ef4124975c9237b917d426f")), // Q
            params
        );

/*        dsa.init(false, pubKey);
        if (!dsa.verifySignature(M, sig[0], sig[1])) {
            println("signature fails")
        }*/
        val signature = encodeToDer(r.toByteArray(), s.toByteArray())

        assertThat(r).isEqualTo(sig[0])
        assertThat(s).isEqualTo(sig[1])
        assertThat(encodeToDer(sig[0].toByteArray(), sig[1].toByteArray())).isEqualTo(signature)
        val signed = sign(priKey.d.toByteArray(), M)
        assertThat(r).isEqualTo(signed.first)
        assertThat(s).isEqualTo(signed.second)


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
        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", byteArrayOf(0x04)+pubKey.q.xCoord.encoded+pubKey.q.yCoord.encoded)
        )
        val match = ellipticCurveSubtleCrypto.nativeVerify(alg, publicKey, signature, M)
        assertThat(match).isTrue()
    }

    @Test
    fun testVectoroldsdk() {
        val x = Base64.decode("7RlJnsuYQuSNdpRAFwejCXZqsAccW_QKWw4dPmABBVA")
        val y = Base64.decode("nf0vn9ib6ObyLm4WaDWUe8g3gkEwo2jVbthS7R4MsaU")
        val d = Base64.decode("2PtA4bb6fXprFLfjIJsi5Cer8YAdEDVDomYNYK9ppkU")
        val data = stringToByteArray("test")
        val sign = sign(d, data)
        val signature = encodeToDer(sign.first.toByteArray(), sign.second.toByteArray())



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
        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", byteArrayOf(0x04)+x+y)
        )
        val match = ellipticCurveSubtleCrypto.nativeVerify(alg, publicKey, signature, hash(byteArrayToString(data)))
        assertThat(match).isTrue()
    }

    @Test
    fun testVectorNewsdk() {
        val x = Base64.decode("7RlJnsuYQuSNdpRAFwejCXZqsAccW_QKWw4dPmABBVA")
        val y = Base64.decode("nf0vn9ib6ObyLm4WaDWUe8g3gkEwo2jVbthS7R4MsaU")
        val d = Base64.decode("2PtA4bb6fXprFLfjIJsi5Cer8YAdEDVDomYNYK9ppkU")
        val data = stringToByteArray("test")
        val sign = sign(d, data)
        val signature = encodeToDer(sign.first.toByteArray(), sign.second.toByteArray())

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
        val publicKey = CryptoKey(
            KeyType.Public,
            true,
            signAlgorithm,
            listOf(KeyUsage.Verify),
            Secp256k1Provider.Secp256k1Handle("", byteArrayOf(0x04)+x+y)
        )
        val match = ellipticCurveSubtleCrypto.nativeVerify(alg, publicKey, signature, hash(byteArrayToString(data)))
        assertThat(match).isTrue()
    }

    fun hash(str: String): ByteArray {
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        return digest.digest(stringToByteArray(str))
    }
}