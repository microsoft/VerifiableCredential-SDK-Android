// Copyright (c) Microsoft Corporation. All rights reserved
package com.microsoft.did.sdk.crypto.plugins

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
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
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.asn1.DLSequence
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.ECPointUtil
import org.spongycastle.jce.interfaces.ECPublicKey
import org.spongycastle.jce.spec.ECNamedCurveSpec
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
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

/*        val verified = ellipticCurveSubtleCrypto.verify(alg, publicKey, signature, stringToByteArray(testData))
        assertThat(verified).isTrue()*/
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
        var verified = false
//        var testData = "this is a random test message for testing signing and verifying"
        var testData = """eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQTJoSVIzZEhtQ1U5U1ZyckQyWDFqZU04NHo5RjBWRlVteU9XTElnWGs4WWc_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRFVtVXlaSEJvYzBWM1duYzNkRVk1YzNGTmMxcHNVbnBLUVdvelEzWTBYMHBsV0ZkdlYxaDVjbGxDUVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVSemIwWnlaa1prWlMxZlJqUjBSR1ExUmxCSmEzSlVRVXRDTjNOM1RHZEJOMkZVZW5KWU1taElSMGxuSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkhOdlJuSm1SbVJsTFY5R05IUkVaRFZHVUVscmNsUkJTMEkzYzNkTVowRTNZVlI2Y2xneWFFaEhTV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWNpMXpYM05wWjI1ZmNXNTNiVGxJTm5OZk1TSXNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl3TVRraUxDSnFkMnNpT25zaWEzUjVJam9pUlVNaUxDSmpjbllpT2lKelpXTndNalUyYXpFaUxDSjRJam9pTTI1elYwdGpMWEZCTm10WU5tUXlSV2xmYVVKcVRVMVlXVEJWY1ZOblFYQjZlWGxOU25Sb1JWaEhaeUlzSW5raU9pSlRORXRQTTBsclZrUXpNMjFrWW04d2NGWmFVRVY1YzJzMlkzaHJXbmxKUTFsVFZWSkJZMk5TZFZGckluMHNJbkIxY25CdmMyVWlPbHNpWVhWMGFDSXNJbWRsYm1WeVlXd2lYWDFkZlgxZGZRI3Itc19zaWduX3Fud205SDZzXzEifQ.eyJpc3MiOiJodHRwczovL3NlbGYtaXNzdWVkLm1lIiwic3ViIjoiWEt5WkJvbFUzaXFuUHg5aEpSRUZGQTdOSUJ4SVJLbXlqRXlZSHpuNFhJTSIsImF1ZCI6Imh0dHBzOi8vcG9ydGFibGVpZGVudGl0eWNhcmRzLmF6dXJlLWFwaS5uZXQvZGV2LXYxLjAvNTM2Mjc5ZjYtMTVjYy00NWYyLWJlMmQtNjFlMzUyYjUxZWVmL3BvcnRhYmxlSWRlbnRpdGllcy9jYXJkL2lzc3VlIiwiZGlkIjoiZGlkOmlvbjpFaUEyaElSM2RIbUNVOVNWcnJEMlgxamVNODR6OUYwVkZVbXlPV0xJZ1hrOFlnPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbERVbVV5WkhCb2MwVjNXbmMzZEVZNWMzRk5jMXBzVW5wS1FXb3pRM1kwWDBwbFdGZHZWMWg1Y2xsQ1FTSXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVUnpiMFp5Wmtaa1pTMWZSalIwUkdRMVJsQkphM0pVUVV0Q04zTjNUR2RCTjJGVWVuSllNbWhJUjBsbkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFJITnZSbkptUm1SbExWOUdOSFJFWkRWR1VFbHJjbFJCUzBJM2MzZE1aMEUzWVZSNmNsZ3lhRWhIU1djaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljaTF6WDNOcFoyNWZjVzUzYlRsSU5uTmZNU0lzSW5SNWNHVWlPaUpGWTJSellWTmxZM0F5TlRack1WWmxjbWxtYVdOaGRHbHZia3RsZVRJd01Ua2lMQ0pxZDJzaU9uc2lhM1I1SWpvaVJVTWlMQ0pqY25ZaU9pSnpaV053TWpVMmF6RWlMQ0o0SWpvaU0yNXpWMHRqTFhGQk5tdFlObVF5UldsZmFVSnFUVTFZV1RCVmNWTm5RWEI2ZVhsTlNuUm9SVmhIWnlJc0lua2lPaUpUTkV0UE0wbHJWa1F6TTIxa1ltOHdjRlphVUVWNWMyczJZM2hyV25sSlExbFRWVkpCWTJOU2RWRnJJbjBzSW5CMWNuQnZjMlVpT2xzaVlYVjBhQ0lzSW1kbGJtVnlZV3dpWFgxZGZYMWRmUSIsInN1Yl9qd2siOnsia3R5IjoiRUMiLCJraWQiOiIjci1zX3NpZ25fcW53bTlINnNfMSIsInVzZSI6InNpZyIsImtleV9vcHMiOlsidmVyaWZ5Il0sImFsZyI6IkVTMjU2SyIsImNydiI6IlAtMjU2SyIsIngiOiIzbnNXS2MtcUE2a1g2ZDJFaV9pQmpNTVhZMFVxU2dBcHp5eU1KdGhFWEdnIiwieSI6IlM0S08zSWtWRDMzbWRibzBwVlpQRXlzazZjeGtaeUlDWVNVUkFjY1J1UWsifSwiaWF0IjoxNTkyMjk1Mjk2LCJleHAiOjE1OTI0NzUyNjQsImNvbnRyYWN0IjoiaHR0cHM6Ly9wb3J0YWJsZWlkZW50aXR5Y2FyZHMuYXp1cmUtYXBpLm5ldC9kZXYtdjEuMC81MzYyNzlmNi0xNWNjLTQ1ZjItYmUyZC02MWUzNTJiNTFlZWYvcG9ydGFibGVJZGVudGl0aWVzL2NvbnRyYWN0cy9JZGVudGl0eUNhcmQiLCJqdGkiOiI1MTFlMDM4ZS04YTJkLTQyZDEtYmVlYS1hMGY4OTIwMzVlZjQiLCJhdHRlc3RhdGlvbnMiOnsiaWRUb2tlbnMiOnsiaHR0cHM6Ly9jdXN0b21lcnNnbG9iYWxseS5iMmNsb2dpbi5jb20vY3VzdG9tZXJzZ2xvYmFsbHkub25taWNyb3NvZnQuY29tL3YyLjAvLndlbGwta25vd24vb3BlbmlkLWNvbmZpZ3VyYXRpb24_cD1CMkNfMV9kZWZhdWx0IjoiZXlKMGVYQWlPaUpLVjFRaUxDSmhiR2NpT2lKU1V6STFOaUlzSW10cFpDSTZJbGcxWlZock5IaDViMnBPUm5WdE1XdHNNbGwwZGpoa2JFNVFOQzFqTlRka1R6WlJSMVJXUW5kaFRtc2lmUS5leUpsZUhBaU9qRTFPVEl5T1RnNE16Z3NJbTVpWmlJNk1UVTVNakk1TlRJek9Dd2lkbVZ5SWpvaU1TNHdJaXdpYVhOeklqb2lhSFIwY0hNNkx5OWpkWE4wYjIxbGNuTm5iRzlpWVd4c2VTNWlNbU5zYjJkcGJpNWpiMjB2Tm1JMFltTXdZMkl0WWprNU55MDBNRFk1TFRsbE5qUXRaamsxTXpnMk5URTJNV0pqTDNZeUxqQXZJaXdpYzNWaUlqb2lOakF3Tm1VNFltRXRaak5oTWkwME9UbGpMVGcwWlRrdE1EVXdOak0wTVRjME9EazNJaXdpWVhWa0lqb2lOR0ZsTkRSbE5XWXRNMlZqTVMwME9XTXhMVGszWVdJdE5ERXdZV0poWVRjMU1HRmpJaXdpYm05dVkyVWlPaUl5TVRReE5UYzNOems0SWl3aWFXRjBJam94TlRreU1qazFNak00TENKaGRYUm9YM1JwYldVaU9qRTFPVEl5T1RVeU16Z3NJbVY0ZEdWdWMybHZibDlEVDFaSlJGOHhPVjlKYlcxMWJtVWlPblJ5ZFdVc0ltVjRkR1Z1YzJsdmJsOUJaMlVpT2lJek1pSXNJbU5wZEhraU9pSlRaV0YwZEd4bElpd2lZMjkxYm5SeWVTSTZJbFZ1YVhSbFpDQlRkR0YwWlhNaUxDSnVZVzFsSWpvaVFXeHBZMlVnVTIxcGRHZ2lMQ0puYVhabGJsOXVZVzFsSWpvaVFXeHBZMlVpTENKbGVIUmxibk5wYjI1ZlIzSmhaSFZoZEdsdmJsOVpaV0Z5SWpveU1ERXlMQ0pxYjJKVWFYUnNaU0k2SWtSdlkzUnZjaUlzSW1WNGRHVnVjMmx2Ymw5TllXcHZjaUk2SWtKcGIyeHZaM2tpTENKbGVIUmxibk5wYjI1ZlRVTkJWRjlUWTI5eVpTSTZJalV5TUNJc0luQnZjM1JoYkVOdlpHVWlPaUk1T0RFd09TSXNJbk4wWVhSbElqb2lWMEVpTENKemRISmxaWFJCWkdSeVpYTnpJam9pTVRJek5DQlVaWEp5ZVNCQmRtVWdUaUlzSW1aaGJXbHNlVjl1WVcxbElqb2lVMjFwZEdnaUxDSjBabkFpT2lKQ01rTmZNVjlrWldaaGRXeDBJbjAuVTVpUTJCbTk2Ym1yUUVOVW1ET2J4NmlGZ0YyYmNoeHdZSzlOQVJVV2ZXVnVxenNHY255UXhHd2g2UGJMbUxGdlFUVi1Fa1ZuQmQxdzJKQ0Z4a0FSSkdnUjVma0VYOXFuV05Kb3FycTY2QmRsUXlfVkluV1lLeDhLOS1FUXNORXdMQ255YWxEY3hQWWdSWTdkR2NHMjBKVElMcmdoRDlQdmx6cFFOQmpUeXA3M0lYZzlCeUJGZmhtR1cxTXN1VlF1bUFvaDBfcHA4MXJIMkNYbVE1YTBRVUlWUjkzeVVDOWZmTVd5LXhRb2JmZkFDVnJZYmpWTUZpVjBFSzdDMXljQlVjOFhTMElFaHkwejM0TzBNc2ktSmJoUWdUZTlkd1NjNHpQTkhzRTlSVENmSkIwNUU0NzRwREhCZ21LMlZrVlhQRTFtYkdFajBMVmpPYWw0ajd4SkxBIn19fQ"""
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
        var signature = ellipticCurveSubtleCrypto.sign(alg, private, stringToByteArray(testData))
        val rs = decodeFromDER(signature)
        val canonicalized = canonicalize(rs.first, rs.second)
        signature = encodeToDer(canonicalized.first.toByteArray(), canonicalized.second.toByteArray())

        verified = ellipticCurveSubtleCrypto.nativeVerify(alg, public, signature, stringToByteArray(testData))
        assertThat(verified).isTrue()
    }

    @Test
    fun verifyTest() {
        val sigstring = "MEQCIGhaF_7-R9g6RqdypGCDG9S_84tYBjx7UsaaWBA4H5Q-AiAWnJYQ-k7cLMfIQ8PjOpqrlIZRN9X-85olVx7ukkiIOQ"
        val signature = Base64Url.decode(sigstring)
//        verified = ellipticCurveSubtleCrypto.nativeVerify(alg, public, signature, stringToByteArray(testData))
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
}