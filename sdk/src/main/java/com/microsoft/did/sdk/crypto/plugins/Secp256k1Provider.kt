// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.util.Constants.SECP256K1_CURVE_NAME_EC
import com.microsoft.did.sdk.util.controlflow.SignatureException
import com.microsoft.did.sdk.util.convertSignedToUnsignedByteArray
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.asn1.DLSequence
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.*
import com.microsoft.did.sdk.crypto.keyStore.KeyStore

class Secp256k1Provider {
    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
    }

    companion object {
        private const val KEY_ALGORITHM = "EC"
        private const val KEY_PROVIDER = "SC"
        private const val SIGNATURE_ALGORITHM = "SHA256WITHPLAIN-ECDSA"
        private const val PROVIDER = "SC"
    }

    fun onGenerateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM, KEY_PROVIDER)
        keyGen.initialize(ECNamedCurveTable.getParameterSpec(SECP256K1_CURVE_NAME_EC))
        return keyGen.genKeyPair()
    }

    fun onSign(data: ByteArray, keyStore: KeyStore, keyId: String): ByteArray {
        val signingKey = keyStore.getKey<PrivateKey>(keyId)
        val signer = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER)
                .apply {
                    initSign(signingKey)
                    update(data)
                }
        return signer.sign()
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        if (signature.size > 64) {
            throw SignatureException("Signature is not in R|S format")
        }
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        val verifySigner = ECDSASigner()
        val ecDomainParameters = createCurveParameters()
        val publicKeyParams = ECPublicKeyParameters(ecDomainParameters.curve.decodePoint(keyData), ecDomainParameters)
        verifySigner.init(false, publicKeyParams)

        return verifySigner.verifySignature(
            hashedData,
            BigInteger(1, signature.sliceArray(0 until signature.size / 2)),
            BigInteger(1, signature.sliceArray(signature.size / 2 until signature.size))
        )
    }

    // NEW CODE

//        val keyGen = KeyPairGenerator.getInstance("EC", "SC")
//        keyGen.initialize(ECNamedCurveTable.getParameterSpec(SECP256K1_CURVE_NAME_EC))
//        val keyPair = keyGen.genKeyPair()
//        val key = keyPair.private
//        key.encoded

    /**
     * Signature is returned as array of BigIntegers for R and S. Converting them into unsigned byte array.
     */
    private fun convertSignatureToUnsignedByteArray(signature: Array<BigInteger>): ByteArray {
        val r = convertSignedToUnsignedByteArray(signature[0].toByteArray())
        val s = convertSignedToUnsignedByteArray(signature[1].toByteArray())
        return r + s
    }

    private fun createCurveParameters(): ECDomainParameters {
        val ecParams = ECNamedCurveTable.getParameterSpec(SECP256K1_CURVE_NAME_EC)
        return ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
    }

    //TODO: Incorporate der encoding while adding support for interoperability
    fun encodeToDer(r: ByteArray, s: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

    fun decodeFromDER(bytes: ByteArray): Pair<BigInteger, BigInteger> {
        val decoder = ASN1InputStream(bytes)
        val seq = decoder.readObject() as DLSequence
        val r = seq.getObjectAt(0) as ASN1Integer
        val s = seq.getObjectAt(1) as ASN1Integer
        return Pair(r.positiveValue, s.positiveValue)
    }

    enum class Secp256k1Tag(val byte: Byte) {
        EVEN(0x02),
        ODD(0x03),
        UNCOMPRESSED(0x04),
        HYBRID_EVEN(0x06),
        HYBRID_ODD(0x07)
    }
}